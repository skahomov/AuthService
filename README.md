ЧТЗ авторизации: https://docs.google.com/document/d/13Q3SblO9EtT7gLlIk-j2WJjyZQhqkilemhzYl5XnOsM/edit?usp=drive_link
Актуальность: 09.11.26

ЧТЗ группы: https://docs.google.com/document/d/1r6wln01RpJHn64jeWsEoGsivWmeTVLN-keZPVA5EMfc/edit?usp=drive_link
Актуальность: 13.11.26

Спецификация(-и): https://docs.google.com/document/d/1OfXEJSaqzQ-3Bl1xqKrCCcxUZfrOHL5NcbYIplVB9l4/edit?usp=sharing
Актуальность: N/A
## Соответствие UUID-миграциям

Во всех пяти основных таблицах ID — UUID; ROLE_ID/USER_ID также UUID.
UUID создаются приложением (UUID.randomUUID), BIGSERIAL/BIGINT в новых миграциях нет.
Три исходных индекса сохранены. В миграции 03 добавлены таблица ROLE_PERMISSIONS
с составным ключом из двух UUID и внешние ключи связей.

AppUser отображает LOGIN, FULL_NAME, PHONE, STATUS, FAILED_LOGIN_ATTEMPTS,
BLOCKED_UNTIL, CREATED_AT, UPDATED_AT; размеры и обязательность полей совпадают с SQL.
Role/Permission также отображают CREATED_AT и UPDATED_AT. В Java-сущности
поле username оставлено ради совместимости API, а в БД оно отображается на LOGIN.
API-поля firstName/lastName вычисляются из FULL_NAME: первое слово и остальная часть.
Учебные ФИО хранятся в порядке «имя фамилия». Отдельных колонок first_name/last_name нет.

ACTIVE — рабочее значение STATUS для начальных пользователей. Любое другое значение
запрещает вход; будущий BLOCKED_UNTIL также запрещает вход. Ошибки пароля увеличивают
FAILED_LOGIN_ATTEMPTS, успешный пароль сбрасывает счётчик. Автоматические пороги
блокировки не вводились: миграции не задают таких правил.

OTP 111111 хранится как BCrypt-хеш в OTP_CODES. Срок — 5 минут в UTC; каждая дошедшая
до сверки хеша попытка увеличивает ATTEMPTS (включая успешную). При успехе USED=true.
Проверка блокирует запись OTP на время транзакции: повторное применение исключено.
Сессия хранит UUID пользователя и конкретной записи OTP, не само значение кода.
Неверный код не откатывает счётчик; отсутствующий/невалидный JSON отсеивается валидацией.
RefreshToken и RefreshTokenRepository соответствуют REFRESH_TOKENS; выпуск и обновление
refresh-токенов не добавлялись, так как ранее такие ручки не входили в задание.

### Применение миграций

Для новой пустой БД используйте обычный запуск ниже: Flyway применит версии
2026.06.11.01, 2026.06.11.02 и 2026.06.11.03, Hibernate выполнит validate.
Миграция создания таблиц исправлена под уточнённое условие UUID.

Если в БД уже применялся прежний вариант с BIGSERIAL или V1__auth_schema.sql,
этот архив не выполняет автоматическое преобразование существующих данных или истории
Flyway. Для такой БД требуется отдельная переходная миграция с сохранением связей и
согласованием flyway_schema_history. Не удаляйте данные и не запускайте repair как
замену преобразованию типов: repair не конвертирует BIGINT в UUID.
Если актуальные UUID-миграции уже были применены, сверяйте их исходные SQL/checksum
с историей перед заменой файлов. Существующие FK требуют отсутствия сиротских ссылок.

В текущем архиве также устранены блокировавшие сборку дубликаты SecurityConfig в gateway,
самоссылка java.version и явно закреплённые несовместимые версии Gateway/Boot plugin.
Фронтенд не изменялся. Healthcheck PostgreSQL использует те же имя базы и пользователя,
что и конфигурация контейнера.

## Выполненные задания AUTH

Добавлены Swagger/OpenAPI в auth-service, PostgreSQL, Flyway, JPA-сущности
пользователя/роли/права/OTP/refresh-токена и репозитории. Login, OTP, `/verify` и `/permissions/check`
обращаются к БД через AuthService. Пароли в БД хранятся как BCrypt-хеши.
Другие контроллеры и CRUD-операции не реализовывались: существующие TODO сохранены.

### Запуск

Требуются JDK 21, Maven и Docker Compose. Все команды — из корня AuthService.

```shell
docker compose up -d --wait
mvn clean package
```

В двух терминалах:

```shell
java -jar auth-service/target/auth-service-0.0.1.jar
```

```shell
java -jar gateway-service/target/gateway-service-0.0.1.jar
```

- auth-service: http://localhost:8081
- gateway: http://localhost:8080
- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI: http://localhost:8081/v3/api-docs
- JWKS: http://localhost:8081/.well-known/jwks.json (также через gateway)
- PostgreSQL: localhost:5434, база/пользователь/локальный пароль `hcc_auth`.

Flyway создаёт таблицы `users`, `roles`, `permissions`,
`role_permissions`, `otp_codes`, `refresh_tokens`; Hibernate проверяет соответствие сущностей схеме.
Пример SQL-проверки после запуска:

```shell
docker compose exec postgres psql -U hcc_auth -d hcc_auth -c "SELECT u.id, u.login, r.name AS role FROM users u JOIN roles r ON r.id = u.role_id;"
```

### Учебные данные

При `AUTH_SEED_DEMO_USERS=true` (локальное значение по умолчанию) создаются только
отсутствующие пользователи, роли и права. Повторный запуск не перезаписывает данные.

| Логин / пароль | Роль | Начальные permissions |
| --- | --- | --- |
| admin / admin | ADMIN | Все четыре права; ADMIN имеет полный доступ |
| supervisor / supervisor | SUPERVISOR | checklist:read, checklist:create, checklist:update |
| technician / technician | TECHNICIAN | checklist:read, checklist:update |
| editor / editor | SUPERVISOR | Как у supervisor; сохранён учебный логин |
| student / student | TECHNICIAN | Как у technician; сохранён учебный логин |

Это локальная начальная матрица. Документ HCC задаёт имена permissions и полный
доступ ADMIN, но не фиксирует права SUPERVISOR/TECHNICIAN. Назначения хранятся в БД
и могут быть изменены в `role_permissions`. CRUD ролей/прав — вне задания.
Для существующей БД перед применением миграции проверьте, что эти таблицы ещё не
созданы вручную; Flyway не изменяет чужую схему и не использует baseline автоматически.

### Проверка запросами (PowerShell)

```powershell
$base = 'http://localhost:8080'
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" -WebSession $session -ContentType 'application/json' -Body '{"username":"admin","password":"admin"}'

$otp = Invoke-WebRequest -Method Post -Uri "$base/api/auth/otp/verify" -WebSession $session -ContentType 'application/json' -Body '{"otp":"111111"}'
$otp.Content
$token = ([string]$otp.Headers['Authorization']) -replace '^Bearer ', ''
$headers = @{ Authorization = "Bearer $token" }
$profile = $otp.Content | ConvertFrom-Json

$verifyBody = @{ token = $token } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/api/auth/verify" -Headers $headers -ContentType 'application/json' -Body $verifyBody

$permissionBody = @{ userId = $profile.id; permission = 'checklist:archive' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/api/auth/permissions/check" -Headers $headers -ContentType 'application/json' -Body $permissionBody
```

Login сверяет пароль с хешем из БД, создаёт запись в otp_codes и сохраняет её UUID в сессии на 5 минут.
Только код `111111` допустим. Неверный код → 401 с `detail`, неверный формат → 400.
Ошибка кода позволяет повторить попытку с той же cookie. Успешное ожидание потребляется
один раз; профиль берётся из БД, access JWT возвращается в заголовке
`Authorization: Bearer <token>`. Тело ответа сохранено как UserProfileResponse.
Токен не сохраняется в browser cookie. Клиент передаёт его в Authorization при
следующих запросах. В Swagger используйте Authorize → serviceBearer с полученным JWT.

`POST /api/auth/verify` принимает контрактный `{ "token": "..." }`, проверяет
подпись, RS256, kid, exp, nbf, issuer, audience и UUID пользователя, затем проверяет
его наличие/активность в БД и возвращает `{ "userId": "...", "role": "ADMIN" }`.
Невалидный токен или неактивный пользователь → 401.

`POST /api/auth/permissions/check` принимает `{ "userId": "UUID", "permission": "checklist:read" }`.
Возвращает `{ "allowed": true/false }` по актуальным назначениям в БД.
Неизвестный/неактивный пользователь или неназначенное право → false; ADMIN → true.
Обе ручки требуют действующий Bearer JWT в заголовке, независимо от тела запроса.

### JWT для интеграции с CORE

RS256, идентификатор ключа `kid` в заголовке. Claims:
`iss`, `sub` (UUID), `aud: ["hcc-logic-service"]`, `iat`, `nbf`, `exp`,
`user_id` (тот же UUID), `permissions` (массив строк), `roles` (массив с именем роли).
CORE проверяет подпись локально по публичным ключам JWKS; вызовы наших `/verify`
и `/permissions/check` для CORE не требуются (они реализованы по заданию).

Настройки CORE для локального auth-service:

```properties
AUTH_ENABLED=true
AUTH_ISSUER_URI=http://localhost:8081
AUTH_JWK_SET_URI=http://localhost:8081/.well-known/jwks.json
AUTH_AUDIENCE=hcc-logic-service
AUTH_USER_ID_CLAIM=user_id
```

В сетевом окружении задайте реальный issuer и доступные CORE URL. Если CORE сам
использует порт 8080, запустите наш gateway на другом порту, например:
`java -jar gateway-service/target/gateway-service-0.0.1.jar --server.port=8088`.

Реализован только необходимый для задания JWT-поток login → OTP → access JWT
и JWKS для проверки подписи. OIDC Authorization Code + PKCE, Discovery,
refresh token и прочие операции из полного плана HCC в эту задачу не входят.
Не объявляйте этот сервис полноценным OIDC Provider и не настраивайте discovery:
для CORE указываются issuer и JWK Set URI явно.

### Конфигурация

| Переменная | Локальное значение по умолчанию |
| --- | --- |
| DB_URL | jdbc:postgresql://localhost:5434/hcc_auth |
| DB_USER / DB_PASSWORD | hcc_auth / hcc_auth |
| AUTH_SEED_DEMO_USERS | true |
| AUTH_ISSUER_URI | http://localhost:8081 |
| AUTH_AUDIENCE | hcc-logic-service |
| AUTH_TOKEN_TTL_SECONDS | 900 |
| AUTH_SIGNING_KEY_FILE | ./.local/auth-signing-keys.json |
| AUTH_ACTIVE_KID | Первый приватный RSA-ключ из файла |
| AUTH_SERVICE_URL (gateway) | http://localhost:8081 |
| AUTH_JWK_SET_URI (gateway/CORE) | http://localhost:8081/.well-known/jwks.json |

Файл ключей — приватный JWK Set. Для локального запуска он создаётся один раз,
сохраняется на диск и повторно используется после перезапуска. В архив и git ключи
не включены; в JWKS выдаются только публичные части. Для общей среды используйте
защищённый постоянный путь и заранее подготовленный JWK Set; отключите demo seed.

Ротация без смены JWT-контракта: добавьте в приватный JWK Set новый RSA-ключ с
уникальным kid, сохраните старые публичные ключи, задайте AUTH_ACTIVE_KID нового
ключа и перезапустите AUTH. Старый публичный ключ сохраняйте минимум до истечения
выданных им токенов и обновления кэша клиентов. Это ручная ротация, нового API нет.

Тесты запускаются `mvn test`. Они проверяют JPA/репозитории и миграцию на H2 в режиме
PostgreSQL, HTTP login/OTP/verify/permissions, Swagger/JWKS и отрицательные сценарии
JWT (подпись, kid, issuer, audience, срок действия, UUID), повторное чтение ключа и ротацию.

Результат проверки: сборка всех модулей и 34 автоматических тестов прошли.
Дополнительно выполнен запуск auth-service + gateway с настоящим PostgreSQL 17:
миграция, заполнение БД, login → OTP → JWT → verify → permissions/check,
публичный JWKS, отказ без Bearer и отсутствие права archive у TECHNICIAN проверены.

Проверка актуальной UUID-схемы: 34 теста, реальный PostgreSQL 17, 3 миграции,
3 исходных индекса, 5 FK, хеш/ATTEMPTS/USED для OTP и HTTP-цепочка через gateway — успешно.
