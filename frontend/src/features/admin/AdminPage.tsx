import AppBar from '@mui/material/AppBar';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import IconButton from '@mui/material/IconButton';
import LinearProgress from '@mui/material/LinearProgress';
import Stack from '@mui/material/Stack';
import Toolbar from '@mui/material/Toolbar';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listUsers } from '../../api/usersApi';
import { useAuth } from '../../auth/AuthContext';
import { useSnackbar } from '../../components/SnackbarProvider';
import { formatFullName, getInitials, ROLE_LABELS } from '../../lib/roles';
import type { User } from '../../types';
import RegisterUserOverlay from './RegisterUserOverlay';
import UserDetailsOverlay from './UserDetailsOverlay';
import UserTable from './UserTable';

export default function AdminPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const showSnack = useSnackbar();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [registerOpen, setRegisterOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    listUsers()
      .then((data) => {
        if (!cancelled) setUsers(data);
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          showSnack(err instanceof Error ? err.message : 'Не удалось загрузить пользователей', 'error');
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [showSnack]);

  /** Оверлей сообщил об изменении пользователя — синхронизируем список и выбранную строку. */
  const handleUserUpdated = (updated: User) => {
    setUsers((previous) => previous.map((item) => (item.id === updated.id ? updated : item)));
    setSelectedUser((previous) => (previous && previous.id === updated.id ? updated : previous));
  };

  const handleUserCreated = (created: User) => {
    setUsers((previous) => [created, ...previous]);
    showSnack(`Пользователь «${created.username}» зарегистрирован`, 'success');
  };

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            HCC Auth · Админка
          </Typography>
          {user && (
            <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.85)' }} noWrap>
                {formatFullName(user)}
              </Typography>
              <Chip
                label={ROLE_LABELS[user.role]}
                size="small"
                variant="outlined"
                sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.6)' }}
              />
              <Avatar sx={{ width: 34, height: 34, bgcolor: 'secondary.main' }}>{getInitials(user)}</Avatar>
              <Tooltip title="Выйти">
                <IconButton size="small" color="inherit" onClick={handleLogout} aria-label="Выйти">
                  <LogoutIcon />
                </IconButton>
              </Tooltip>
            </Stack>
          )}
        </Toolbar>
      </AppBar>

      <Box sx={{ maxWidth: 1040, mx: 'auto', p: 3 }}>
        <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              Сотрудники
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Всего: {users.length}
            </Typography>
          </Box>
          <Button
            variant="contained"
            startIcon={<PersonAddIcon />}
            onClick={() => setRegisterOpen(true)}
          >
            Зарегистрировать пользователя
          </Button>
        </Stack>

        {loading && <LinearProgress sx={{ mb: 1 }} />}

        <UserTable users={users} loading={loading} onSelect={setSelectedUser} />
      </Box>

      <UserDetailsOverlay
        user={selectedUser}
        onClose={() => setSelectedUser(null)}
        onUserUpdated={handleUserUpdated}
      />
      <RegisterUserOverlay
        open={registerOpen}
        onClose={() => setRegisterOpen(false)}
        onCreated={handleUserCreated}
      />
    </Box>
  );
}
