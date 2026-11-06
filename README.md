ЧТЗ авторизации: https://docs.google.com/document/d/13Q3SblO9EtT7gLlIk-j2WJjyZQhqkilemhzYl5XnOsM/edit?usp=drive_link
Актуальность: 09.11.26

ЧТЗ группы: https://docs.google.com/document/d/1r6wln01RpJHn64jeWsEoGsivWmeTVLN-keZPVA5EMfc/edit?usp=drive_link
Актуальность: 13.11.26

Спецификация(-и): https://docs.google.com/document/d/1OfXEJSaqzQ-3Bl1xqKrCCcxUZfrOHL5NcbYIplVB9l4/edit?usp=sharing
Актуальность: N/A
## Учебный gateway и BFF

Требуются JDK 21 и Maven. Из корня проекта:

```shell
mvn clean package
```

Запустите в двух терминалах:

```shell
java -jar auth-service/target/auth-service-1.0-SNAPSHOT.jar
```

```shell
java -jar gateway-service/target/gateway-service-1.0-SNAPSHOT.jar
```

Gateway: http://localhost:8080; auth-service: http://localhost:8081.
Маршрут `/api/auth/**` сохраняет исходный путь. Адрес auth-service можно изменить
переменной окружения `AUTH_SERVICE_URL`.
В gateway ровно один Java-файл: `SecurityConfig.java`; он также содержит точку запуска.

Пример проверки (в Windows используйте `curl.exe`):

```shell
curl -i -c cookies.txt -H "Content-Type: application/json" -d '{"username":"editor","password":"editor"}' http://localhost:8080/api/auth/login
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"otp":"anything"}' http://localhost:8080/api/auth/otp/verify
```

Учётные записи: editor/editor → EDITOR, student/student → STUDENT.
Login возвращает OTP_REQUIRED, неверные данные — 401. Cookie AUTHSESSION связывает
OTP с пользователем. Без успешного login verifyOtp возвращает 401; пустой OTP — 400.
Любой непустой OTP принимается; после успешного verifyOtp ожидание кода удаляется.
Профиль содержит username, role и стабильный учебный UUID; остальные поля — null.
Это только два учебных шага: создание SecurityContext после OTP не реализовано.
Остальные методы BffController выбрасывают UnsupportedOperationException("TODO").
Gateway разрешает без аутентификации только login и otp/verify и игнорирует для них CSRF;
остальные маршруты требуют аутентификации, CSRF для них остаётся включённым.

В контракте разрешены короткие учебные пароли и произвольный непустой OTP,
в UserRole добавлены EDITOR и STUDENT. Из зависимости auth-service на контракт
исключены JPA и security starter, чтобы не требовалась БД и не включалась вторая
автоматическая цепочка безопасности. Security находится в gateway.
