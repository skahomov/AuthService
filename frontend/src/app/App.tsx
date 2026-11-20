import { CssBaseline, ThemeProvider } from '@mui/material';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { getToken, getStoredUser } from '../auth/tokenStore';
import { SnackbarProvider } from '../components/SnackbarProvider';
import AdminPage from '../features/admin/AdminPage';
import LoginPage from '../features/login/LoginPage';
import OtpPage from '../features/otp/OtpPage';
import { RequireAuth } from './RequireAuth';
import { theme } from './theme';

/** «/» отправляет на админку, если сессия есть, иначе на вход. */
function HomeRedirect() {
  const authenticated = getToken() !== null && getStoredUser() !== null;
  return <Navigate to={authenticated ? '/admin' : '/login'} replace />;
}

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SnackbarProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/otp" element={<OtpPage />} />
              <Route
                path="/admin"
                element={
                  <RequireAuth>
                    <AdminPage />
                  </RequireAuth>
                }
              />
              <Route path="*" element={<HomeRedirect />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </SnackbarProvider>
    </ThemeProvider>
  );
}
