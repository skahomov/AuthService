import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { getToken, getStoredUser } from '../auth/tokenStore';

/** Защищённый маршрут: без сохранённой сессии — редирект на вход. */
export function RequireAuth({ children }: { children: ReactNode }) {
  const location = useLocation();
  const authenticated = getToken() !== null && getStoredUser() !== null;

  if (!authenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <>{children}</>;
}
