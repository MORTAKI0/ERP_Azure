const REFRESH_TOKEN_KEY = 'mini-erp.refreshToken';
const REFRESH_EXP_KEY = 'mini-erp.refreshExp';

function getSessionStorage(): Storage | null {
  try {
    return window.sessionStorage;
  } catch {
    return null;
  }
}

export class AuthSession {
  static getRefreshToken(): string | null {
    return getSessionStorage()?.getItem(REFRESH_TOKEN_KEY) ?? null;
  }

  static getRefreshExpiryEpochMs(): number | null {
    const rawValue = getSessionStorage()?.getItem(REFRESH_EXP_KEY);
    if (!rawValue) {
      return null;
    }

    const numericValue = Number(rawValue);
    return Number.isFinite(numericValue) ? numericValue : null;
  }

  static setRefreshToken(refreshToken: string, refreshExpiresInSeconds?: number): void {
    const storage = getSessionStorage();
    if (!storage) {
      return;
    }

    storage.setItem(REFRESH_TOKEN_KEY, refreshToken);

    if (typeof refreshExpiresInSeconds === 'number') {
      storage.setItem(REFRESH_EXP_KEY, String(Date.now() + refreshExpiresInSeconds * 1000));
    } else {
      storage.removeItem(REFRESH_EXP_KEY);
    }
  }

  static clear(): void {
    const storage = getSessionStorage();
    if (!storage) {
      return;
    }

    storage.removeItem(REFRESH_TOKEN_KEY);
    storage.removeItem(REFRESH_EXP_KEY);
  }
}
