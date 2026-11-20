/**
 * Типы, зеркалящие контракты из модуля authContract
 * (edu.pd.contract.dto). Держим полями строго те имена,
 * которые использует бэкенд в JSON.
 */

/** Enum UserRole на бэкенде — фиксированный набор значений. */
export type UserRole = 'ADMIN' | 'SUPERVISOR' | 'TECHNICIAN';

/** Ответ POST /api/auth/login */
export interface LoginResponse {
  status: string;
  message: string;
  /** Замаскированный номер телефона (бэк пока возвращает null). */
  maskedPhone: string | null;
}

/** Профиль пользователя: тело ответа otp/verify, /me и registration. */
export interface UserProfileResponse {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: UserRole;
}

/**
 * Тело POST /api/auth/registration.
 * ⚠️ Поле отчества в бэкенде называется `pavetry` — опечатка в контракте,
 * но отправлять нужно именно это имя.
 */
export interface RegistrationRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  pavetry?: string;
  phone: string;
  role: UserRole;
}

/** Ошибка валидации конкретного поля (edu.pd.contract.dto.ErrorResponse.FieldError). */
export interface FieldErrorDto {
  field: string;
  rejectedValue?: unknown;
  message: string;
}

/** Единый формат ошибок бэкенда (edu.pd.contract.dto.ErrorResponse). */
export interface ErrorResponse {
  status?: number;
  type?: string;
  title?: string;
  detail?: string;
  instance?: string;
  timestamp?: string;
  fieldErrors?: FieldErrorDto[];
}

/** Право доступа (например, `checklist:read`). */
export interface Permission {
  id: string;
  name: string;
}

/**
 * Пользователь для админки: профиль + служебные поля,
 * которых пока нет в контракте (список пользователей бэкенд не отдаёт — см. моки).
 */
export interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  pavetry?: string;
  phone: string;
  role: UserRole;
  enabled: boolean;
}
