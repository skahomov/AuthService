import type { User, UserRole } from '../types';

/** Человеческие названия ролей. */
export const ROLE_LABELS: Record<UserRole, string> = {
  ADMIN: 'Администратор',
  SUPERVISOR: 'Супервизор',
  TECHNICIAN: 'Техник',
};

/** Цвет чипа роли в таблице/шапке. */
export const ROLE_CHIP_COLOR: Record<UserRole, 'error' | 'primary' | 'success'> = {
  ADMIN: 'error',
  SUPERVISOR: 'primary',
  TECHNICIAN: 'success',
};

/** Порядок ролей в селекторах. */
export const ROLE_OPTIONS: UserRole[] = ['ADMIN', 'SUPERVISOR', 'TECHNICIAN'];

/** ФИО одной строкой: фамилия имя отчество (отчество — если указано). */
export function formatFullName(user: Pick<User, 'lastName' | 'firstName' | 'pavetry'>): string {
  return [user.lastName, user.firstName, user.pavetry].filter(Boolean).join(' ');
}

/** Инициалы для аватара. */
export function getInitials(user: Pick<User, 'lastName' | 'firstName'>): string {
  const last = user.lastName?.trim()?.charAt(0) ?? '';
  const first = user.firstName?.trim()?.charAt(0) ?? '';
  return (last + first).toUpperCase() || '?';
}
