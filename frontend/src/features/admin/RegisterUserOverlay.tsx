import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { ApiError } from '../../api/client';
import { registerUser } from '../../api/usersApi';
import { ROLE_LABELS, ROLE_OPTIONS } from '../../lib/roles';
import type { RegistrationRequest, User } from '../../types';

interface RegisterUserOverlayProps {
  open: boolean;
  onClose: () => void;
  onCreated: (user: User) => void;
}

/** Маска телефона из контракта RegistrationRequest. */
const PHONE_PATTERN = /^\+?[0-9]{10,15}$/;

const EMPTY_FORM: RegistrationRequest = {
  username: '',
  password: '',
  firstName: '',
  lastName: '',
  pavetry: '',
  phone: '',
  role: 'TECHNICIAN',
};

/**
 * Регистрация пользователя: логин, пароль, ФИО, телефон, роль.
 * Валидация на клиенте зеркальна контракту; ошибки поля с бэкенда
 * (ErrorResponse.fieldErrors) маппятся на соответствующие TextField.
 */
export default function RegisterUserOverlay({ open, onClose, onCreated }: RegisterUserOverlayProps) {
  const [formError, setFormError] = useState<string | null>(null);
  const {
    control,
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<RegistrationRequest>({ defaultValues: EMPTY_FORM });

  const handleClose = () => {
    reset(EMPTY_FORM);
    setFormError(null);
    onClose();
  };

  const submit = handleSubmit(async (values) => {
    setFormError(null);
    try {
      const created = await registerUser(values);
      onCreated(created);
      handleClose();
    } catch (error) {
      if (error instanceof ApiError) {
        const assignable = new Set([
          'username',
          'password',
          'firstName',
          'lastName',
          'pavetry',
          'phone',
          'role',
        ]);
        let mapped = false;
        for (const fieldError of error.fieldErrors) {
          if (assignable.has(fieldError.field)) {
            setError(fieldError.field as keyof RegistrationRequest, { message: fieldError.message });
            mapped = true;
          }
        }
        if (!mapped) setFormError(error.message);
      } else if (error instanceof Error) {
        setFormError(error.message);
      }
    }
  });

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ fontWeight: 700 }}>Регистрация пользователя</DialogTitle>
      <Box component="form" onSubmit={submit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            {formError && <Alert severity="error">{formError}</Alert>}

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                required
                fullWidth
                label="Имя"
                error={!!errors.firstName}
                helperText={errors.firstName?.message ?? ' '}
                {...register('firstName', { required: 'Обязательное поле' })}
              />
              <TextField
                required
                fullWidth
                label="Фамилия"
                error={!!errors.lastName}
                helperText={errors.lastName?.message ?? ' '}
                {...register('lastName', { required: 'Обязательное поле' })}
              />
            </Stack>

            <TextField
              fullWidth
              label="Отчество (необязательно)"
              error={!!errors.pavetry}
              helperText={errors.pavetry?.message ?? ' '}
              {...register('pavetry')}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                required
                fullWidth
                label="Логин"
                error={!!errors.username}
                helperText={errors.username?.message ?? ' '}
                {...register('username', {
                  required: 'Обязательное поле',
                  minLength: { value: 3, message: 'Минимум 3 символа' },
                  maxLength: { value: 100, message: 'Максимум 100 символов' },
                })}
              />
              <TextField
                required
                fullWidth
                label="Пароль"
                type="password"
                error={!!errors.password}
                helperText={errors.password?.message ?? ' '}
                {...register('password', {
                  required: 'Обязательное поле',
                  minLength: { value: 8, message: 'Минимум 8 символов' },
                  maxLength: { value: 128, message: 'Максимум 128 символов' },
                })}
              />
            </Stack>

            <TextField
              required
              fullWidth
              label="Номер телефона"
              placeholder="+79991234567"
              error={!!errors.phone}
              helperText={errors.phone?.message ?? 'Формат: +79991234567 (10–15 цифр)'}
              {...register('phone', {
                required: 'Обязательное поле',
                pattern: { value: PHONE_PATTERN, message: 'Формат: +79991234567 (10–15 цифр)' },
              })}
            />

            <Controller
              name="role"
              control={control}
              rules={{ required: 'Выберите роль' }}
              render={({ field, fieldState }) => (
                <TextField
                  select
                  required
                  fullWidth
                  label="Роль"
                  error={!!fieldState.error}
                  helperText={fieldState.error?.message ?? ' '}
                  {...field}
                >
                  {ROLE_OPTIONS.map((role) => (
                    <MenuItem key={role} value={role}>
                      {ROLE_LABELS[role]}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={handleClose} color="inherit">
            Отмена
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={isSubmitting}
            startIcon={isSubmitting ? <CircularProgress size={18} color="inherit" /> : undefined}
          >
            Зарегистрировать
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
}
