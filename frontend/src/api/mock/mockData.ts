import type { Permission, User, UserRole } from '../../types';

/**
 * Мок-данные админки. Роли и права повторяют сид-данные бэкенда
 * (edu.pd.authservice.config.DatabaseConfig): ADMIN — все права,
 * SUPERVISOR — read/create/update, TECHNICIAN — read/update.
 */

export const ALL_PERMISSIONS: Permission[] = [
  { id: 'c9a5a6f0-1e2b-4f3c-8a7d-5b6e7f800001', name: 'checklist:read' },
  { id: 'c9a5a6f0-1e2b-4f3c-8a7d-5b6e7f800002', name: 'checklist:create' },
  { id: 'c9a5a6f0-1e2b-4f3c-8a7d-5b6e7f800003', name: 'checklist:update' },
  { id: 'c9a5a6f0-1e2b-4f3c-8a7d-5b6e7f800004', name: 'checklist:archive' },
];

export const INITIAL_ROLE_PERMISSIONS: Record<UserRole, string[]> = {
  ADMIN: ['checklist:read', 'checklist:create', 'checklist:update', 'checklist:archive'],
  SUPERVISOR: ['checklist:read', 'checklist:create', 'checklist:update'],
  TECHNICIAN: ['checklist:read', 'checklist:update'],
};

export const MOCK_USERS: User[] = [
  {
    id: 'd1a2b3c4-0001-4c5d-9e6f-102938475a01',
    username: 'admin',
    firstName: 'Иван',
    lastName: 'Иванов',
    pavetry: 'Иванович',
    phone: '+79951002030',
    role: 'ADMIN',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-0002-4c5d-9e6f-102938475a02',
    username: 'supervisor',
    firstName: 'Ольга',
    lastName: 'Смирнова',
    pavetry: 'Николаевна',
    phone: '+79951002031',
    role: 'SUPERVISOR',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-0003-4c5d-9e6f-102938475a03',
    username: 'technician',
    firstName: 'Пётр',
    lastName: 'Кузнецов',
    phone: '+79951002032',
    role: 'TECHNICIAN',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-0004-4c5d-9e6f-102938475a04',
    username: 'editor',
    firstName: 'Мария',
    lastName: 'Орлова',
    pavetry: 'Андреевна',
    phone: '+79951002033',
    role: 'SUPERVISOR',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-0005-4c5d-9e6f-102938475a05',
    username: 'student',
    firstName: 'Алексей',
    lastName: 'Сидоров',
    pavetry: 'Дмитриевич',
    phone: '+79951002034',
    role: 'TECHNICIAN',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-0006-4c5d-9e6f-102938475a06',
    username: 'petrov',
    firstName: 'Артём',
    lastName: 'Петров',
    pavetry: 'Викторович',
    phone: '+79951002035',
    role: 'TECHNICIAN',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-0007-4c5d-9e6f-102938475a07',
    username: 'vasileva',
    firstName: 'Дарья',
    lastName: 'Васильева',
    phone: '+79951002036',
    role: 'SUPERVISOR',
    enabled: false,
  },
  {
    id: 'd1a2b3c4-0008-4c5d-9e6f-102938475a08',
    username: 'morozov',
    firstName: 'Егор',
    lastName: 'Морозов',
    pavetry: 'Павлович',
    phone: '+79951002037',
    role: 'TECHNICIAN',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-0009-4c5d-9e6f-102938475a09',
    username: 'nikolaeva',
    firstName: 'Анна',
    lastName: 'Николаева',
    pavetry: 'Сергеевна',
    phone: '+79951002038',
    role: 'SUPERVISOR',
    enabled: true,
  },
  {
    id: 'd1a2b3c4-000a-4c5d-9e6f-102938475a0a',
    username: 'zaitsev',
    firstName: 'Роман',
    lastName: 'Зайцев',
    pavetry: 'Олегович',
    phone: '+79951002039',
    role: 'TECHNICIAN',
    enabled: false,
  },
];
