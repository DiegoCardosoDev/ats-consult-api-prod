import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req.clone({ withCredentials: true }));
};

export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      const isAuthRequest = req.url.includes('/auth/');
      if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthRequest) {
        localStorage.removeItem('is_logged');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
