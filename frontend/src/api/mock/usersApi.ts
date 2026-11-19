import type { RegistrationRequest, User, UserRole } from '../../types';
import { INITIAL_ROLE_PERMISSIONS, MOCK_USERS } from './mockData';

/**
 * Мок-реализация операций админки. Живёт в памяти модуля:
 * переживает переходы между экранами, сбрасывается при перезагрузке страницы.
 * Когда на бэкенде появятся ручки (список пользователей, set-role, set-permits),
 * фасад src/api/usersApi.ts переключится на реальные вызовы.
 */

const delay = (ms = 300) => new Promise<void>((resolve) => setTimeout(resolve, ms));

let users: User[] = MOCK_USERS.map((user) => ({ ...user }));

let rolePermissions: Record<UserRole, string[]> = {
  ADMIN: [...INITIAL_ROLE_PERMISSIONS.ADMIN],
  SUPERVISOR: [...INITIAL_ROLE_PERMISSIONS.SUPERVISOR],
  TECHNICIAN: [...INITIAL_ROLE_PERMISSIONS.TECHNICIAN],
};

export async function listUsers(): Promise<User[]> {
  await delay();
  return users.map((user) => ({ ...user }));
}

export async function setRole(userId: string, role: UserRole): Promise<User> {
  await delay();
  const user = users.find((candidate) => candidate.id === userId);
  if (!user) throw new Error('Пользователь не найден');
  user.role = role;
  return { ...user };
}

export async function getRolePermissions(role: UserRole): Promise<string[]> {
  await delay(200);
  return [...(rolePermissions[role] ?? [])];
}

export async function setRolePermissions(role: UserRole, permissions: string[]): Promise<string[]> {
  await delay();
  rolePermissions[role] = [...permissions];
  return [...rolePermissions[role]];
}

export async function registerUser(request: RegistrationRequest): Promise<User> {
  await delay();
  if (users.some((user) => user.username.toLowerCase() === request.username.toLowerCase())) {
    throw new Error(`Пользователь с логином «${request.username}» уже существует`);
  }
  const user: User = {
    id: crypto.randomUUID(),
    username: request.username,
    firstName: request.firstName,
    lastName: request.lastName,
    pavetry: request.pavetry,
    phone: request.phone,
    role: request.role,
    enabled: true,
  };
  users = [user, ...users];
  return { ...user };
}
