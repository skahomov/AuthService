import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#3f51b5' },
    secondary: { main: '#00897b' },
    background: { default: '#f4f6fb' },
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: '"Segoe UI", system-ui, -apple-system, sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { textTransform: 'none', fontWeight: 600 },
      },
    },
    MuiCheckbox: {
      styleOverrides: {
        // Чекбокс наследует shape.borderRadius (10) и выглядит почти круглым —
        // возвращаем привычную квадратную форму.
        root: { borderRadius: 4 },
      },
    },
  },
});
