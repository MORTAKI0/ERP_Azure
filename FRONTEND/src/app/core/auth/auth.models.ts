export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  expiresInSeconds: number;
  refreshToken: string;
  refreshExpiresInSeconds: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  expiresInSeconds: number;
}

export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiErrorEnvelope {
  timestamp: string;
  status: number;
  error:
    | 'unauthorized'
    | 'forbidden'
    | 'invalid_credentials'
    | 'invalid_refresh_token'
    | 'validation_failed'
    | 'not_found'
    | 'conflict'
    | string;
  message: string;
  path: string;
  correlationId?: string;
  fieldErrors?: ApiFieldError[];
}

export interface AppApiError {
  kind: 'api' | 'network';
  status: number;
  message: string;
  envelope?: ApiErrorEnvelope;
}
