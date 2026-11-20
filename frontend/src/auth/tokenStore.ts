import type { UserProfileResponse } from '../types';

/**
 * Хранение сессии на клиенте. JWT живёт 15 минут и приходит
 * в заголовке ответа /api/auth/otp/verify — cookie его не содержит,
 * поэтому держим токен в sessionStorage (переживает F5, чистится по выходу).
 * Cookie AUTHSESSION (httpOnly) браузер отправляет сам — она нужна
 * для шага login → otp/verify.
 */

const TOKEN_KEY = 'hcc.auth.token';
const USER_KEY = 'hcc.auth.user';
const OTP_PENDING_KEY = 'hcc.auth.otpPending';

export function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  sessionStorage.setItem(TOKEN_KEY, token);
}

export function getStoredUser(): UserProfileResponse | null {
  const raw = sessionStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as UserProfileResponse;
  } catch {
    return null;
  }
}

export function setStoredUser(user: UserProfileResponse): void {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function setOtpPending(pending: boolean): void {
  if (pending) {
    sessionStorage.setItem(OTP_PENDING_KEY, '1');
  } else {
    sessionStorage.removeItem(OTP_PENDING_KEY);
  }
}

export function isOtpPending(): boolean {
  return sessionStorage.getItem(OTP_PENDING_KEY) === '1';
}

export function clearSession(): void {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
  sessionStorage.removeItem(OTP_PENDING_KEY);
}
