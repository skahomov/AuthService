import Chip from '@mui/material/Chip';
import InputAdornment from '@mui/material/InputAdornment';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableFooter from '@mui/material/TableFooter';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import SearchIcon from '@mui/icons-material/Search';
import { useMemo, useState } from 'react';
import { formatFullName, ROLE_CHIP_COLOR, ROLE_LABELS } from '../../lib/roles';
import type { User } from '../../types';

interface UserTableProps {
  users: User[];
  loading: boolean;
  onSelect: (user: User) => void;
}

const COLUMNS = ['ФИО', 'Логин', 'Телефон', 'Роль', 'Статус'] as const;

export default function UserTable({ users, loading, onSelect }: UserTableProps) {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return users;
    return users.filter((user) =>
      [user.username, user.phone, formatFullName(user)].some((value) =>
        value.toLowerCase().includes(normalized),
      ),
    );
  }, [users, query]);

  const maxPage = Math.max(0, Math.ceil(filtered.length / rowsPerPage) - 1);
  const safePage = Math.min(page, maxPage);
  const pageRows = filtered.slice(safePage * rowsPerPage, safePage * rowsPerPage + rowsPerPage);

  return (
    <Stack spacing={1.5}>
      <TextField
        size="small"
        placeholder="Поиск по ФИО, логину или телефону"
        value={query}
        onChange={(event) => {
          setQuery(event.target.value);
          setPage(0);
        }}
        sx={{ maxWidth: 380 }}
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon fontSize="small" color="action" />
              </InputAdornment>
            ),
          },
        }}
      />

      <TableContainer component={Paper} variant="outlined">
        <Table size="medium">
          <TableHead>
            <TableRow sx={{ bgcolor: 'action.hover' }}>
              {COLUMNS.map((column) => (
                <TableCell key={column} sx={{ fontWeight: 700 }}>
                  {column}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {pageRows.map((user) => (
              <TableRow
                key={user.id}
                hover
                onClick={() => onSelect(user)}
                sx={{ cursor: 'pointer' }}
              >
                <TableCell>{formatFullName(user)}</TableCell>
                <TableCell>{user.username}</TableCell>
                <TableCell>{user.phone}</TableCell>
                <TableCell>
                  <Chip size="small" color={ROLE_CHIP_COLOR[user.role]} label={ROLE_LABELS[user.role]} />
                </TableCell>
                <TableCell>
                  <Chip
                    size="small"
                    variant="outlined"
                    color={user.enabled ? 'success' : 'default'}
                    label={user.enabled ? 'Активен' : 'Заблокирован'}
                  />
                </TableCell>
              </TableRow>
            ))}
            {pageRows.length === 0 && (
              <TableRow>
                <TableCell colSpan={COLUMNS.length}>
                  <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 4 }}>
                    {loading ? 'Загрузка…' : 'Пользователи не найдены'}
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
          <TableFooter>
            <TableRow>
              <TablePagination
                count={filtered.length}
                page={safePage}
                rowsPerPage={rowsPerPage}
                rowsPerPageOptions={[5, 10, 25]}
                labelRowsPerPage="Строк:"
                labelDisplayedRows={({ from, to, count }) => `${from}–${to} из ${count}`}
                onPageChange={(_event, newPage) => setPage(newPage)}
                onRowsPerPageChange={(event) => {
                  setRowsPerPage(Number(event.target.value));
                  setPage(0);
                }}
              />
            </TableRow>
          </TableFooter>
        </Table>
      </TableContainer>
    </Stack>
  );
}
