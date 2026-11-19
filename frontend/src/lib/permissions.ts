/**
 * Человеческие названия прав. Ключ — техническое имя права из бэкенда:
 * его менять нельзя, бэкенд сравнивает точные строки (permissions/check).
 */
export const PERMISSION_LABELS: Record<string, string> = {
  'checklist:read': 'Просмотр чеклистов',
  'checklist:create': 'Создание чеклистов',
  'checklist:update': 'Редактирование чеклистов',
  'checklist:archive': 'Архивирование чеклистов',
};

/** Подпись права: человекочитаемая, для неизвестных прав — техническое имя. */
export function permissionLabel(name: string): string {
  return PERMISSION_LABELS[name] ?? name;
}
