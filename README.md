ЧТЗ авторизации: https://docs.google.com/document/d/13Q3SblO9EtT7gLlIk-j2WJjyZQhqkilemhzYl5XnOsM/edit?usp=drive_link
Актуальность: 09.11.26

ЧТЗ группы: https://docs.google.com/document/d/1r6wln01RpJHn64jeWsEoGsivWmeTVLN-keZPVA5EMfc/edit?usp=drive_link
Актуальность: 13.11.26

Спецификация(-и): https://docs.google.com/document/d/1OfXEJSaqzQ-3Bl1xqKrCCcxUZfrOHL5NcbYIplVB9l4/edit?usp=sharing
Актуальность: N/A
## Проверка OTP

Требуются JDK 21 и Maven. Из корня: `mvn clean package`.
Запуск в двух терминалах:

```shell
java -jar auth-service/target/auth-service-1.0-SNAPSHOT.jar
java -jar gateway-service/target/gateway-service-1.0-SNAPSHOT.jar
```

Gateway: http://localhost:8080; auth-service: http://localhost:8081.
Единственный допустимый OTP: строка `111111` (без пробелов).
Сначала выполните login и сохраните cookie AUTHSESSION, затем передайте её при verifyOtp.
Например в PowerShell:

```powershell
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -WebSession $session -ContentType 'application/json' -Body '{"username":"admin","password":"admin"}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/otp/verify -WebSession $session -ContentType 'application/json' -Body '{"otp":"111111"}'
```

- `111111` после login: 200 и UserProfileResponse.
- Другой шестизначный код: 401 и `detail: "Неверный OTP-код"`.
- Пустой, отсутствующий код, неверный формат или некорректный JSON: 400 с понятным сообщением.
- Правильный код без предварительного login: 401.
- После ошибки кода можно повторить попытку с той же cookie; после успеха ожидание OTP удаляется.

Обработчик BffExceptionHandler возвращает контрактный ErrorResponse, включая detail и
fieldErrors для ошибок валидации. Код и пароль в ответах об ошибке не повторяются.
Пользователи и роли сохранены из актуального архива. Остальные операции остаются TODO.
В SecurityConfig восстановлена точка запуска gateway и добавлен throws Exception,
необходимый для компиляции SecurityFilterChain. В pom.xml gateway восстановлен
Spring Cloud BOM 2025.1.0 для разрешения версии gateway starter.

Проверки: `mvn test` (из корня).

Проверено: сборка всех модулей, 19 тестов OTP (без ошибок), HTTP-запросы через gateway.
