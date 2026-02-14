import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStore } from './auth-store';

const AUTH_PATHS = ['/auth/login', '/auth/refresh', '/auth/logout'];

function isAuthRequest(url: string): boolean {
  return AUTH_PATHS.some((path) => url.includes(path));
}

export const authHeaderInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);

  if (isAuthRequest(req.url)) {
    return next(req);
  }

  const accessToken = authStore.accessToken();
  if (!accessToken) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    })
  );
};
