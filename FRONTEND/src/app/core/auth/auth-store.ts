import { computed, Injectable, signal } from '@angular/core';
import { AppApiError, LoginResponse, RefreshTokenResponse } from './auth.models';
import { AuthSession } from './auth-session';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  readonly accessToken = signal<string | null>(null);
  readonly accessExpEpochMs = signal<number | null>(null);
  readonly loading = signal(false);
  readonly lastError = signal<AppApiError | null>(null);

  readonly isAuthenticated = computed(() => !!this.accessToken());
  readonly hasRefreshToken = computed(() => !!AuthSession.getRefreshToken());

  setLoading(loading: boolean): void {
    this.loading.set(loading);
  }

  setError(error: AppApiError | null): void {
    this.lastError.set(error);
  }

  setTokensFromLogin(loginResponse: LoginResponse): void {
    this.accessToken.set(loginResponse.accessToken);
    this.accessExpEpochMs.set(Date.now() + loginResponse.expiresInSeconds * 1000);
    AuthSession.setRefreshToken(loginResponse.refreshToken, loginResponse.refreshExpiresInSeconds);
    this.lastError.set(null);
  }

  setAccessFromRefresh(refreshResponse: RefreshTokenResponse): void {
    this.accessToken.set(refreshResponse.accessToken);
    this.accessExpEpochMs.set(Date.now() + refreshResponse.expiresInSeconds * 1000);
    this.lastError.set(null);
  }

  clearSession(): void {
    this.accessToken.set(null);
    this.accessExpEpochMs.set(null);
    this.lastError.set(null);
    this.loading.set(false);
    AuthSession.clear();
  }
}
