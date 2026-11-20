import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CircularProgress from '@mui/material/CircularProgress';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { useRef, useState, type KeyboardEvent as ReactKeyboardEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { useAuth } from '../../auth/AuthContext';

const OTP_LENGTH = 6;

/**
 * Шаг 2 входа: ввод OTP-кода. Сессия (cookie AUTHSESSION) уже создана
 * на /login и живёт 5 минут; код подтверждения — фикс. 111111.
 * Успешный ответ несёт JWT в заголовке Authorization.
 */
export default function OtpPage() {
  const { user, otpPending, verifyOtp, cancelOtp } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const maskedPhone = (location.state as { maskedPhone?: string | null } | null)?.maskedPhone ?? null;

  const [digits, setDigits] = useState<string[]>(() => Array<string>(OTP_LENGTH).fill(''));
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const inputRefs = useRef<Array<HTMLInputElement | null>>([]);

  if (user) return <Navigate to="/admin" replace />;
  if (!otpPending) return <Navigate to="/login" replace />;

  const focusAt = (index: number) => {
    inputRefs.current[Math.max(0, Math.min(index, OTP_LENGTH - 1))]?.focus();
  };

  const setDigitAt = (index: number, value: string) => {
    setDigits((previous) => {
      const next = [...previous];
      next[index] = value;
      return next;
    });
  };

  const handleChange = (index: number, raw: string) => {
    const digitsOnly = raw.replace(/\D/g, '');
    if (!digitsOnly) {
      setDigitAt(index, '');
      return;
    }
    if (digitsOnly.length > 1) {
      // Вставка всего кода в любое из полей
      const chars = digitsOnly.slice(0, OTP_LENGTH).split('');
      setDigits((previous) => {
        const next = [...previous];
        chars.forEach((char, position) => {
          next[position] = char;
        });
        return next;
      });
      focusAt(chars.length);
      return;
    }
    setDigitAt(index, digitsOnly);
    if (index < OTP_LENGTH - 1) focusAt(index + 1);
  };

  const handleKeyDown = (index: number, event: ReactKeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Backspace') {
      if (!digits[index] && index > 0) {
        event.preventDefault();
        setDigitAt(index - 1, '');
        focusAt(index - 1);
      }
    } else if (event.key === 'ArrowLeft' && index > 0) {
      focusAt(index - 1);
    } else if (event.key === 'ArrowRight' && index < OTP_LENGTH - 1) {
      focusAt(index + 1);
    } else if (event.key === 'Enter') {
      void submit();
    }
  };

  const submit = async () => {
    if (submitting) return;
    const otp = digits.join('');
    if (otp.length !== OTP_LENGTH) {
      setError('Введите все шесть цифр кода');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await verifyOtp(otp);
      navigate('/admin', { replace: true });
    } catch (err) {
      if (err instanceof ApiError) setError(err.message);
      else if (err instanceof Error) setError(err.message);
      setDigits(Array<string>(OTP_LENGTH).fill(''));
      focusAt(0);
    } finally {
      setSubmitting(false);
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
      <Card sx={{ width: '100%', maxWidth: 480, boxShadow: 6 }}>
        <CardContent sx={{ p: 4 }}>
          <Stack spacing={2.5}>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>
                Подтверждение входа
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {maskedPhone
                  ? `Код подтверждения отправлен на номер ${maskedPhone}`
                  : 'Введите шестизначный код подтверждения'}
              </Typography>
            </Box>

            {error && (
              <Alert severity="error" onClose={() => setError(null)}>
                {error}
              </Alert>
            )}

            <Stack direction="row" spacing={1} sx={{ justifyContent: 'center' }}>
              {digits.map((digit, index) => (
                <TextField
                  key={index}
                  value={digit}
                  disabled={submitting}
                  onChange={(event) => handleChange(index, event.target.value)}
                  inputRef={(element) => {
                    inputRefs.current[index] = element;
                  }}
                  slotProps={{
                    htmlInput: {
                      maxLength: 1,
                      inputMode: 'numeric',
                      autoComplete: 'one-time-code',
                      style: { textAlign: 'center', fontSize: 24, padding: '8px 0' },
                      onKeyDown: (event: ReactKeyboardEvent<HTMLInputElement>) =>
                        handleKeyDown(index, event),
                    },
                  }}
                  sx={{ width: 52 }}
                />
              ))}
            </Stack>

            <Button
              variant="contained"
              size="large"
              onClick={() => void submit()}
              disabled={submitting || digits.some((digit) => !digit)}
              startIcon={submitting ? <CircularProgress size={20} color="inherit" /> : undefined}
            >
              Подтвердить
            </Button>

            <Button
              color="inherit"
              onClick={() => {
                cancelOtp();
                navigate('/login', { replace: true });
              }}
            >
              ← Вернуться к вводу логина
            </Button>

            <Typography variant="caption" color="text.secondary" align="center">
              Демо-код: 111111 · сессия для ввода кода живёт 5 минут
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
