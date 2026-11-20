import { clearSession, getToken } from '../auth/tokenStore';
import type { ErrorResponse } from '../types';

/**
 * Пути, где 401 — часть бизнес-логики (неверный пароль/код, ещё нет сессии),
 * а не «JWT истёк»: на них редирект на /login не выполняется.
 */
const NO_REDIRECT_PATHS = new Set(['/api/auth/login', '/api/auth/otp/verify', '/api/auth/logout']);

/** Ошибка API с разобранным телом ErrorResponse (edu.pd.contract.dto.ErrorResponse). */
export class ApiError extends Error {
  readonly status: number;
  readonly response?: ErrorResponse;

  constructor(status: number, response?: ErrorResponse) {
    super(response?.detail || response?.title || `Ошибка запроса (${status})`);
    this.name = 'ApiError';
    this.status = status;
    this.response = response;
  }

  get fieldErrors() {
    return this.response?.fieldErrors ?? [];
  }
}

interface ApiResult<T> {
  data: T | null;
  response: Response;
}

/**
 * Базовый HTTP-клиент. Все запросы идут через прокси Vite на gateway (:8080),
 * поэтому это same-origin: cookie AUTHSESSION уходит автоматически,
 * CORS не требуется.
 */
export async function api<T>(path: string, init: RequestInit = {}): Promise<ApiResult<T>> {
  const headers = new Headers(init.headers);
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(path, { ...init, headers, credentials: 'include' });

  if (response.ok) {
    const data = response.status === 204 ? null : await parseJson<T>(response);
    return { data, response };
  }

  const errorResponse = (await parseJson<ErrorResponse>(response).catch(() => null)) ?? undefined;

  // Сессия истекла/токен невалиден — сбрасываем и отправляем на вход.
  if (response.status === 401 && !NO_REDIRECT_PATHS.has(path)) {
    clearSession();
    window.location.assign('/login?expired=1');
  }

  throw new ApiError(response.status, errorResponse);
}

async function parseJson<T>(response: Response): Promise<T | null> {
  const text = await response.text();
  if (!text) return null;
  return JSON.parse(text) as T;
}
