import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, finalize, map, of, switchMap, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface AuthUser {
  id: number;
  name: string | null;
  email: string;
  role: 'ADMIN' | 'TECH_RECRUITER' | 'USER';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiBaseUrl}/auth`;
  private readonly currentUserState = signal<AuthUser | null>(null);

  readonly currentUser = this.currentUserState.asReadonly();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(credentials: { email: string; password: string }): Observable<AuthUser> {
    return this.ensureCsrfToken().pipe(
      switchMap(() => this.http.post<unknown>(`${this.apiUrl}/login`, credentials)),
      switchMap(() => this.me())
    );
  }

  register(credentials: { name: string; email: string; password: string }): Observable<AuthUser> {
    return this.ensureCsrfToken().pipe(
      switchMap(() => this.http.post<unknown>(`${this.apiUrl}/register`, credentials)),
      switchMap(() => this.me())
    );
  }

  me(): Observable<AuthUser> {
    return this.http.get<AuthUser>(`${this.apiUrl}/me`).pipe(
      tap(user => this.currentUserState.set(user))
    );
  }

  ensureSession(): Observable<boolean> {
    if (this.currentUserState()) {
      return of(true);
    }

    return this.me().pipe(
      map(() => true),
      catchError(() => {
        this.clearSession();
        return of(false);
      })
    );
  }

  isAuthenticated(): boolean {
    return this.currentUserState() !== null;
  }

  isStaff(): boolean {
    const role = this.currentUserState()?.role;
    return role === 'ADMIN' || role === 'TECH_RECRUITER';
  }

  isCandidate(): boolean {
    return this.currentUserState()?.role === 'USER';
  }

  clearSession(): void {
    this.currentUserState.set(null);
  }

  logout(): void {
    this.performLogout()
      .pipe(finalize(() => this.finishLogout()))
      .subscribe({ error: () => undefined });
  }

  private performLogout(): Observable<unknown> {
    return this.http.post<unknown>(`${this.apiUrl}/logout`, {}).pipe(
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === 403) {
          return this.ensureCsrfToken().pipe(
            switchMap(() => this.http.post<unknown>(`${this.apiUrl}/logout`, {}))
          );
        }
        return throwError(() => error);
      })
    );
  }

  private ensureCsrfToken(): Observable<void> {
    return this.http.get<void>(`${this.apiUrl}/csrf`);
  }

  private finishLogout(): void {
    this.clearSession();
    if (this.router.url !== '/login') {
      this.router.navigate(['/login']);
    }
  }
}
