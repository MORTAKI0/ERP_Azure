import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AUTH_BASE_URL } from '../../core/config/api-endpoints';
import {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse
} from '../../core/auth/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly authBaseUrl = `${AUTH_BASE_URL}/auth`;

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authBaseUrl}/login`, request);
  }

  refresh(request: RefreshTokenRequest): Observable<RefreshTokenResponse> {
    return this.http.post<RefreshTokenResponse>(`${this.authBaseUrl}/refresh`, request);
  }

  logout(request: RefreshTokenRequest): Observable<void> {
    return this.http.post<void>(`${this.authBaseUrl}/logout`, request);
  }
}
