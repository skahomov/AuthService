import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CircularProgress from '@mui/material/CircularProgress';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { useAuth } from '../../auth/AuthContext';

interface LoginFormValues {
  username: string;
  password: string;
}

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [formError, setFormError] = useState<string | null>(() =>
    searchParams.get('expired') ? 'Сессия истекла. Войдите заново.' : null,
  );
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>();

  const onSubmit = async (values: LoginFormValues) => {
    setFormError(null);
    try {
      await login(values.username, values.password);
      navigate('/otp', { replace: true });
    } catch (error) {
      if (error instanceof ApiError) {
        // 401/400 с ErrorResponse: detail или ошибки полей
        let mapped = false;
        for (const fieldError of error.fieldErrors) {
          if (fieldError.field === 'username' || fieldError.field === 'password') {
            setError(fieldError.field as keyof LoginFormValues, { message: fieldError.message });
            mapped = true;
          }
        }
        if (!mapped) setFormError(error.message);
      } else if (error instanceof Error) {
        setFormError(error.message);
      }
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: 2,
      }}
    >
      <Card sx={{ width: '100%', maxWidth: 420, boxShadow: 6 }}>
        <CardContent sx={{ p: 4 }}>
          <Stack spacing={2.5} component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>
                Вход в систему
              </Typography>
              <Typography variant="body2" color="text.secondary">
                HCC Auth · двухфакторная аутентификация
              </Typography>
            </Box>

            {formError && <Alert severity="error">{formError}</Alert>}

            <TextField
              label="Логин"
              fullWidth
              autoFocus
              error={!!errors.username}
              helperText={errors.username?.message ?? ' '}
              {...register('username', { required: 'Введите логин' })}
            />
            <TextField
              label="Пароль"
              type="password"
              fullWidth
              error={!!errors.password}
              helperText={errors.password?.message ?? ' '}
              {...register('password', { required: 'Введите пароль' })}
            />

            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={isSubmitting}
              startIcon={isSubmitting ? <CircularProgress size={20} color="inherit" /> : undefined}
            >
              Войти
            </Button>

            <Typography variant="caption" color="text.secondary" align="center">
              Демо-доступ: admin / admin, OTP-код 111111
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
