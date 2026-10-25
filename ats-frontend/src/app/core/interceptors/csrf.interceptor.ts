import { HttpClient, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';

const CSRF_COOKIE = 'XSRF-TOKEN';
const CSRF_HEADER = 'X-XSRF-TOKEN';
const MUTATING_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  if (!MUTATING_METHODS.has(req.method) || !req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  const token = readCookie(CSRF_COOKIE);
  if (token) {
    return next(addCsrfHeader(req, token));
  }

  return inject(HttpClient).get<void>(`${environment.apiBaseUrl}/auth/csrf`).pipe(
    switchMap(() => {
      const refreshedToken = readCookie(CSRF_COOKIE);
      return next(refreshedToken ? addCsrfHeader(req, refreshedToken) : req);
    })
  );
};

function addCsrfHeader(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({
    headers: req.headers.set(CSRF_HEADER, token)
  });
}

function readCookie(name: string): string | null {
  if (typeof document === 'undefined') {
    return null;
  }

  const prefix = `${name}=`;
  const cookie = document.cookie
    .split(';')
    .map(item => item.trim())
    .find(item => item.startsWith(prefix));

  return cookie ? decodeURIComponent(cookie.substring(prefix.length)) : null;
}
