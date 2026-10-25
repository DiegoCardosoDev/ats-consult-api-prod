import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, switchMap, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiBaseUrl}/auth`;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(credentials: { email: string; password: string }): Observable<unknown> {
    return this.ensureCsrfToken().pipe(
      switchMap(() => this.http.post(`${this.apiUrl}/login`, credentials)),
      tap(() => localStorage.setItem('is_logged', 'true'))
    );
  }

  register(credentials: { email: string; password: string }): Observable<unknown> {
    return this.ensureCsrfToken().pipe(
      switchMap(() => this.http.post(`${this.apiUrl}/register`, credentials)),
      tap(() => localStorage.setItem('is_logged', 'true'))
    );
  }

  logout(): void {
    this.performLogout().subscribe({
      next: () => this.finishLogout(),
      error: (error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.finishLogout();
        }
      }
    });
  }

  private performLogout(): Observable<unknown> {
    return this.ensureCsrfToken().pipe(
      switchMap(() => this.http.post(`${this.apiUrl}/logout`, {})),
      catchError((error: HttpErrorResponse) => {
        if (error.status !== 403) {
          return throwError(() => error);
        }
        return this.ensureCsrfToken().pipe(
          switchMap(() => this.http.post(`${this.apiUrl}/logout`, {}))
        );
      })
    );
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('is_logged') === 'true';
  }

  private ensureCsrfToken(): Observable<void> {
    return this.http.get<void>(`${this.apiUrl}/csrf`);
  }

  private finishLogout(): void {
    localStorage.removeItem('is_logged');
    this.router.navigate(['/login']);
  }
}
