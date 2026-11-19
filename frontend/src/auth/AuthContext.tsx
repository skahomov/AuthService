import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import * as authApi from '../api/authApi';
import {
  clearSession,
  getStoredUser,
  isOtpPending,
  setOtpPending,
  setStoredUser,
  setToken,
} from './tokenStore';
import type { LoginResponse, UserProfileResponse } from '../types';

interface AuthContextValue {
  /** Профиль вошедшего пользователя (null — не авторизован). */
  user: UserProfileResponse | null;
  /** Пройден login, ожидается ввод OTP. */
  otpPending: boolean;
  login: (username: string, password: string) => Promise<LoginResponse>;
  verifyOtp: (otp: string) => Promise<void>;
  cancelOtp: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfileResponse | null>(() => getStoredUser());
  const [otpPending, setOtpPendingState] = useState<boolean>(() => !getStoredUser() && isOtpPending());

  /** Шаг 1: логин/пароль → сервер кладёт pending-пользователя в сессию (cookie). */
  const login = useCallback(async (username: string, password: string) => {
    const response = await authApi.login(username, password);
    setOtpPending(true);
    setOtpPendingState(true);
    return response;
  }, []);

  /** Шаг 2: OTP-код → JWT (хранится на клиенте) + профиль. */
  const verifyOtp = useCallback(async (otp: string) => {
    const { profile, token } = await authApi.otpVerify(otp);
    setToken(token);
    setStoredUser(profile);
    setOtpPending(false);
    setOtpPendingState(false);
    setUser(profile);
  }, []);

  const cancelOtp = useCallback(() => {
    setOtpPending(false);
    setOtpPendingState(false);
  }, []);

  const logout = useCallback(() => {
    // Ручка logout на бэке пока TODO — ошибку игнорируем, локально выходим всегда.
    void authApi.logoutRemote().catch(() => undefined);
    clearSession();
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ user, otpPending, login, verifyOtp, cancelOtp, logout }),
    [user, otpPending, login, verifyOtp, cancelOtp, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth должен использоваться внутри AuthProvider');
  }
  return context;
}
