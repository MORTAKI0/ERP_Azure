import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { catchError, finalize, mapTo, shareReplay, tap } from 'rxjs/operators';
import { AuthApi } from '../../features/authentication/auth-api';
import { AppApiError, RefreshTokenResponse } from './auth.models';
import { AuthSession } from './auth-session';
import { AuthStore } from './auth-store';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApi);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  private refreshInFlight$: Observable<RefreshTokenResponse> | null = null;

  login(email: string, password: string): Observable<void> {
    this.authStore.setLoading(true);
    this.authStore.setError(null);

    return this.authApi.login({ email, password }).pipe(
      tap((response) => this.authStore.setTokensFromLogin(response)),
      mapTo(void 0),
      catchError((error) => {
        this.authStore.setError(error as AppApiError);
        return throwError(() => error);
      }),
      finalize(() => this.authStore.setLoading(false))
    );
  }

  refresh(): Observable<RefreshTokenResponse> {
    const refreshToken = AuthSession.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('missing_refresh_token'));
    }

    return this.authApi.refresh({ refreshToken }).pipe(
      tap((response) => this.authStore.setAccessFromRefresh(response))
    );
  }

  refreshSingleFlight(): Observable<RefreshTokenResponse> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }

    this.refreshInFlight$ = this.refresh().pipe(
      shareReplay({ bufferSize: 1, refCount: false }),
      finalize(() => {
        this.refreshInFlight$ = null;
      })
    );

    return this.refreshInFlight$;
  }

  logout(): Observable<void> {
    const refreshToken = AuthSession.getRefreshToken();
    this.authStore.clearSession();

    if (!refreshToken) {
      return of(void 0);
    }

    return this.authApi.logout({ refreshToken }).pipe(
      catchError(() => of(void 0))
    );
  }

  expireSessionAndRedirect(): void {
    this.authStore.clearSession();
    void this.router.navigate(['/login'], {
      queryParams: { reason: 'session_expired' }
    });
  }
}
