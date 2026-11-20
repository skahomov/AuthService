import type { RegistrationRequest, User, UserRole } from '../types';
import { registrationRemote } from './authApi';
import {
  getRolePermissions as mockGetRolePermissions,
  listUsers as mockListUsers,
  registerUser as mockRegisterUser,
  setRole as mockSetRole,
  setRolePermissions as mockSetRolePermissions,
} from './mock/usersApi';

/**
 * Фасад операций админки. VITE_USE_MOCKS=true — данные из моков
 * (эндпоинты списка пользователей/ролей/прав на бэкенде отсутствуют,
 * а set-role и set-permits объявлены в контракте, но не реализованы).
 */

const USE_MOCKS = import.meta.env.VITE_USE_MOCKS === 'true';

/** Список всех пользователей. Бэкенд: ручки пока нет (контракт в работе). */
export async function listUsers(): Promise<User[]> {
  return mockListUsers();
}

/**
 * Смена роли пользователя.
 * Бэкенд (контракт): POST /api/auth/users/{id}/set-role/{roleId} — TODO.
 */
export async function setRole(userId: string, role: UserRole): Promise<User> {
  return mockSetRole(userId, role);
}

/** Права конкретной роли. Бэкенд: ручки чтения ролей/прав нет. */
export async function getRolePermissions(role: UserRole): Promise<string[]> {
  return mockGetRolePermissions(role);
}

/**
 * Заменить набор прав роли.
 * Бэкенд (контракт): POST /api/auth/role/{id}/set-permits — TODO.
 */
export async function setRolePermissions(role: UserRole, permissions: string[]): Promise<string[]> {
  return mockSetRolePermissions(role, permissions);
}

/** Регистрация пользователя: мок или реальный POST /api/auth/registration. */
export async function registerUser(request: RegistrationRequest): Promise<User> {
  if (USE_MOCKS) {
    return mockRegisterUser(request);
  }
  const profile = await registrationRemote(request);
  return { ...profile, enabled: true };
}
