# HCC Auth — фронтенд

React-приложение для AuthService: вход с двухфакторной аутентификацией (логин/пароль → OTP) и админка с управлением сотрудниками.

## Стек

- **Vite + React 19 + TypeScript**
- **MUI v7** — компоненты (таблица, оверлеи, формы)
- **react-router-dom** — маршрутизация
- **react-hook-form** — формы и валидация

## Запуск

Нужен запущенный бэкенд:

```bash
# 1. PostgreSQL (порт хоста 5434)
docker compose up -d --wait

# 2. auth-service (:8081)
java -jar auth-service/target/auth-service-*.jar

# 3. gateway-service (:8080)
java -jar gateway-service/target/gateway-service-*.jar
```

Фронтенд:

```bash
cd frontend
npm install
npm run dev
```

Откроется `http://localhost:5173`. Все запросы `/api/*` проксируются Vite на gateway `http://localhost:8080` — CORS не нужен.

**Демо-доступ:** логин `admin`, пароль `admin`, OTP-код `111111`.

## Режим моков

`.env.development`:

```
VITE_USE_MOCKS=true
```

- `true` — списки пользователей, смена роли, права ролей и регистрация работают на мок-данных в памяти. Логин и OTP при этом всё равно идут на реальный бэкенд.
- `false` — все операции идут на реальный бэкенд.

Моки нужны, потому что на бэкенде сейчас не реализованы: `GET /users` (нет даже в контракте), `registration`, `set-role`, `set-permits`, `logout` (в контракте есть — в коде TODO).

## Структура

```
src/
├── api/            HTTP-клиент (Bearer, ErrorResponse), authApi, usersApi-фасад
│   └── mock/       мок-данные и мок-реализация операций админки
├── auth/           AuthContext, хранение JWT в sessionStorage
├── app/            App (роуты), тема MUI, RequireAuth
├── components/     SnackbarProvider
├── features/
│   ├── login/      экран входа
│   ├── otp/        экран ввода OTP (6 полей)
│   └── admin/      таблица сотрудников, оверлей сотрудника, оверлей регистрации
├── lib/            роли (лейблы, цвета), форматирование
└── types/          контракты из authContract (включая опечатку `pavetry`)
```

## Поток аутентификации

1. `POST /api/auth/login` — сервер создаёт сессию (cookie `AUTHSESSION`, 5 минут).
2. `POST /api/auth/otp/verify` — при верном коде возвращает профиль и **JWT в заголовке `Authorization`**; дальше все запросы идут с `Authorization: Bearer`.
3. JWT живёт 15 минут, refresh-токена нет: при 401 клиент сбрасывает сессию и отправляет на `/login?expired=1`.

## Известные ограничения бэкенда (актуально для реального режима)

- JWT не обновляется — через 15 минут потребуется повторный вход.
- `maskedPhone` в ответе login пока всегда `null` — маска телефона не показывается.
- Ручки `registration`, `set-role`, `set-permits`, `logout`, `lock/unlock` — TODO (500).
- Списков пользователей/ролей/прав нет ни в коде, ни в контракте.
