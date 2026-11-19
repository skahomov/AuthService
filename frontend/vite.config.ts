import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Прокси на gateway-service: CORS не нужен, cookie AUTHSESSION остаётся same-origin.
    // auth-service ждёт по адресу http://localhost:8081, gateway — на http://localhost:8080.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/.well-known': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
