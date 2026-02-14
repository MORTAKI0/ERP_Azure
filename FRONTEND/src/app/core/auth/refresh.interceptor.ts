import {
  HttpContextToken,
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { AuthStore } from './auth-store';

const AUTH_PATHS = ['/auth/login', '/auth/refresh', '/auth/logout'];
const RETRIED_ONCE = new HttpContextToken<boolean>(() => false);

function isAuthRequest(url: string): boolean {
  return AUTH_PATHS.some((path) => url.includes(path));
}

export const refreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);
  const authService = inject(AuthService);

  if (isAuthRequest(req.url)) {
    return next(req);
  }

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      if (req.context.get(RETRIED_ONCE) || !authStore.hasRefreshToken()) {
        return throwError(() => error);
      }

      return authService.refreshSingleFlight().pipe(
        catchError((refreshError: unknown) => {
          authService.expireSessionAndRedirect();
          return throwError(() => refreshError);
        }),
        switchMap(() =>
          next(
            req.clone({
              context: req.context.set(RETRIED_ONCE, true)
            })
          )
        )
      );
    })
  );
};
