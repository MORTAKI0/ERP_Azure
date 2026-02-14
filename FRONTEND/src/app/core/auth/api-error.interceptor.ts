import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ApiErrorEnvelope, AppApiError } from './auth.models';

function looksLikeApiEnvelope(value: unknown): value is ApiErrorEnvelope {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const candidate = value as Partial<ApiErrorEnvelope>;
  return typeof candidate.status === 'number' && typeof candidate.error === 'string';
}

export const apiErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      if (error.status === 0) {
        const networkError: AppApiError = {
          kind: 'network',
          status: 0,
          message: 'Network error. Please check your connection and retry.'
        };
        return throwError(() => networkError);
      }

      const envelope = looksLikeApiEnvelope(error.error) ? error.error : undefined;
      const normalizedError: AppApiError = {
        kind: 'api',
        status: error.status,
        message: envelope?.message ?? error.message,
        envelope
      };

      if (error.status === 403) {
        void router.navigate(['/access-denied']);
      }

      return throwError(() => normalizedError);
    })
  );
};
