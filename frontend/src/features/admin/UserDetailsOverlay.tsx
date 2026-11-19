import Alert from '@mui/material/Alert';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Checkbox from '@mui/material/Checkbox';
import CircularProgress from '@mui/material/CircularProgress';
import Divider from '@mui/material/Divider';
import Drawer from '@mui/material/Drawer';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormGroup from '@mui/material/FormGroup';
import IconButton from '@mui/material/IconButton';
import LinearProgress from '@mui/material/LinearProgress';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import CloseIcon from '@mui/icons-material/Close';
import { useEffect, useState } from 'react';
import { getRolePermissions, setRole, setRolePermissions } from '../../api/usersApi';
import { ALL_PERMISSIONS } from '../../api/mock/mockData';
import { useSnackbar } from '../../components/SnackbarProvider';
import { permissionLabel } from '../../lib/permissions';
import { formatFullName, getInitials, ROLE_LABELS, ROLE_OPTIONS } from '../../lib/roles';
import type { User, UserRole } from '../../types';

interface UserDetailsOverlayProps {
  user: User | null;
  onClose: () => void;
  onUserUpdated: (user: User) => void;
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" spacing={2} sx={{ justifyContent: 'space-between' }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 600, textAlign: 'right', wordBreak: 'break-all' }}>
        {value}
      </Typography>
    </Stack>
  );
}

/**
 * Оверлей сотрудника: профиль + селектор смены роли + права роли.
 * ВАЖНО: права принадлежат роли (Role ←→ Permission, ManyToMany),
 * поэтому их изменение затрагивает всех сотрудников с этой ролью.
 */
export default function UserDetailsOverlay({ user, onClose, onUserUpdated }: UserDetailsOverlayProps) {
  const showSnack = useSnackbar();
  const [selectedRole, setSelectedRole] = useState<UserRole | ''>('');
  const [rolePermissions, setRolePermissionsState] = useState<string[]>([]);
  const [permissionsLoading, setPermissionsLoading] = useState(false);
  const [savingRole, setSavingRole] = useState(false);
  const [savingPermissions, setSavingPermissions] = useState(false);

  useEffect(() => {
    if (!user) return;
    setSelectedRole(user.role);
    let cancelled = false;
    setPermissionsLoading(true);
    getRolePermissions(user.role)
      .then((permissions) => {
        if (!cancelled) setRolePermissionsState(permissions);
      })
      .catch(() => {
        if (!cancelled) showSnack('Не удалось загрузить права роли', 'error');
      })
      .finally(() => {
        if (!cancelled) setPermissionsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [user, showSnack]);

  const handleRoleChange = async (nextRole: UserRole) => {
    if (!user) return;
    const previousRole = user.role;
    setSelectedRole(nextRole);
    if (nextRole === previousRole) return;
    setSavingRole(true);
    try {
      const updated = await setRole(user.id, nextRole);
      showSnack(`Роль изменена на «${ROLE_LABELS[nextRole]}»`, 'success');
      onUserUpdated(updated);
      const permissions = await getRolePermissions(nextRole);
      setRolePermissionsState(permissions);
    } catch (err) {
      showSnack(err instanceof Error ? err.message : 'Не удалось изменить роль', 'error');
      setSelectedRole(previousRole);
    } finally {
      setSavingRole(false);
    }
  };

  const handlePermissionToggle = async (permissionName: string, checked: boolean) => {
    if (!user) return;
    const previous = rolePermissions;
    const next = checked
      ? [...previous, permissionName]
      : previous.filter((name) => name !== permissionName);
    setRolePermissionsState(next); // оптимистично
    setSavingPermissions(true);
    try {
      await setRolePermissions(user.role, next);
      showSnack('Права роли обновлены', 'success');
    } catch (err) {
      showSnack(err instanceof Error ? err.message : 'Не удалось обновить права', 'error');
      setRolePermissionsState(previous); // откат
    } finally {
      setSavingPermissions(false);
    }
  };

  return (
    <Drawer
      anchor="right"
      open={user !== null}
      onClose={onClose}
      sx={{ '& .MuiDrawer-paper': { width: { xs: '100vw', sm: 460 } } }}
    >
      {user && (
        <Box sx={{ p: 3, display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Avatar sx={{ width: 52, height: 52, bgcolor: 'primary.main', fontSize: 18 }}>
              {getInitials(user)}
            </Avatar>
            <Box sx={{ flexGrow: 1, minWidth: 0 }}>
              <Typography variant="subtitle1" noWrap sx={{ fontWeight: 700 }}>
                {formatFullName(user)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                @{user.username}
              </Typography>
            </Box>
            <IconButton onClick={onClose} aria-label="Закрыть">
              <CloseIcon />
            </IconButton>
          </Stack>

          <Divider />

          <Stack spacing={1.2}>
            <DetailRow label="Телефон" value={user.phone} />
            <DetailRow label="Статус" value={user.enabled ? 'Активен' : 'Заблокирован'} />
            <DetailRow label="ID" value={user.id} />
          </Stack>

          <Divider />

          <Box>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
              Роль
            </Typography>
            <TextField
              select
              fullWidth
              size="small"
              label="Роль пользователя"
              value={selectedRole}
              disabled={savingRole}
              onChange={(event) => void handleRoleChange(event.target.value as UserRole)}
            >
              {ROLE_OPTIONS.map((role) => (
                <MenuItem key={role} value={role}>
                  {ROLE_LABELS[role]}
                </MenuItem>
              ))}
            </TextField>
            {savingRole && <LinearProgress sx={{ mt: 1 }} />}
          </Box>

          <Divider />

          <Box>
            <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="subtitle2">Права роли «{ROLE_LABELS[user.role]}»</Typography>
              {savingPermissions && <CircularProgress size={16} />}
            </Stack>
            <Alert severity="warning" variant="outlined" sx={{ mb: 1.5 }}>
              Права принадлежат роли: изменения действуют на всех сотрудников с ролью «
              {ROLE_LABELS[user.role]}».
            </Alert>
            {permissionsLoading ? (
              <LinearProgress />
            ) : (
              <FormGroup>
                {ALL_PERMISSIONS.map((permission) => (
                  <FormControlLabel
                    key={permission.id}
                    control={
                      <Checkbox
                        checked={rolePermissions.includes(permission.name)}
                        onChange={(event) =>
                          void handlePermissionToggle(permission.name, event.target.checked)
                        }
                      />
                    }
                    label={
                      <Box>
                        <Typography variant="body2">{permissionLabel(permission.name)}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {permission.name}
                        </Typography>
                      </Box>
                    }
                  />
                ))}
              </FormGroup>
            )}
          </Box>
        </Box>
      )}
    </Drawer>
  );
}
