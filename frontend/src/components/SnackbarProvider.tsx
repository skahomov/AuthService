import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import Alert from '@mui/material/Alert';
import Snackbar from '@mui/material/Snackbar';
import type { AlertColor } from '@mui/material';

interface SnackState {
  message: string;
  severity: AlertColor;
  key: number;
}

type ShowSnackbar = (message: string, severity?: AlertColor) => void;

const SnackbarContext = createContext<ShowSnackbar>(() => undefined);

export function SnackbarProvider({ children }: { children: ReactNode }) {
  const [snack, setSnack] = useState<SnackState | null>(null);

  const show = useCallback<ShowSnackbar>((message, severity = 'success') => {
    setSnack({ message, severity, key: Date.now() });
  }, []);

  const close = () => setSnack(null);

  return (
    <SnackbarContext.Provider value={show}>
      {children}
      <Snackbar
        key={snack?.key}
        open={snack !== null}
        autoHideDuration={4000}
        onClose={close}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={snack?.severity ?? 'info'} variant="filled" onClose={close} sx={{ minWidth: 280 }}>
          {snack?.message}
        </Alert>
      </Snackbar>
    </SnackbarContext.Provider>
  );
}

/** Показать всплывающее уведомление: useSnackbar()('Текст', 'error'). */
export function useSnackbar(): ShowSnackbar {
  return useContext(SnackbarContext);
}
