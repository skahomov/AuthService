import { api } from './client';
import type { LoginResponse, RegistrationRequest, UserProfileResponse } from '../types';

/**
 * Реальные ручки бэкенда (BffController). Cookie AUTHSESSION устанавливается
 * сервером на /login и автоматически уходит с /otp/verify — same-origin через прокси.
 */

/** POST /api/auth/login → { status: "OTP_REQUIRED", message, maskedPhone } */
export async function login(username: string, password: string): Promise<LoginResponse> {
  const { data } = await api<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  if (!data) throw new Error('Пустой ответ сервера при входе');
  return data;
}

/**
 * POST /api/auth/otp/verify → тело: UserProfileResponse,
 * JWT приходит в заголовке ответа `Authorization: Bearer <token>`.
 */
export async function otpVerify(otp: string): Promise<{ profile: UserProfileResponse; token: string }> {
  const { data, response } = await api<UserProfileResponse>('/api/auth/otp/verify', {
    method: 'POST',
    body: JSON.stringify({ otp }),
  });
  const authHeader = response.headers.get('Authorization');
  if (!data || !authHeader?.startsWith('Bearer ')) {
    throw new Error('Сервер не вернул токен авторизации');
  }
  return { profile: data, token: authHeader.slice('Bearer '.length) };
}

/** POST /api/auth/registration → 201, UserProfileResponse. На бэке пока TODO (500). */
export async function registrationRemote(request: RegistrationRequest): Promise<UserProfileResponse> {
  const { data } = await api<UserProfileResponse>('/api/auth/registration', {
    method: 'POST',
    body: JSON.stringify(request),
  });
  if (!data) throw new Error('Пустой ответ сервера при регистрации');
  return data;
}

/**
 * POST /api/auth/logout — на бэке TODO (500): вызываем, но ошибку игнорируем,
 * локальную сессию сбрасываем в любом случае.
 */
export async function logoutRemote(): Promise<void> {
  await api('/api/auth/logout', { method: 'POST' });
}
