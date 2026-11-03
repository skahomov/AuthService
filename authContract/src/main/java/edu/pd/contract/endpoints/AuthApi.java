package edu.pd.contract.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.UUID;

import edu.pd.contract.dto.ErrorResponse;
import edu.pd.contract.dto.LoginRequest;
import edu.pd.contract.dto.LoginResponse;
import edu.pd.contract.dto.OtpVerifyRequest;
import edu.pd.contract.dto.PermissionCheckRequest;
import edu.pd.contract.dto.PermissionCheckResponse;
import edu.pd.contract.dto.RegistrationRequest;
import edu.pd.contract.dto.UserProfileResponse;
import edu.pd.contract.dto.VerifyTokenRequest;
import edu.pd.contract.dto.VerifyTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(
        name = "Auth",
        description = "Сервис авторизации: проверка JWT/прав и BFF-операции аутентификации"
)
@RequestMapping(
        value = "/api/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface AuthApi {

    String SECURITY_SCHEME_SERVICE = "serviceBearer";
    String SECURITY_SCHEME_SESSION = "sessionCookie";

    @Operation(
            summary = "Проверить JWT токен",
            description = "Сервис-сервис операция. Проверяет JWT токен и возвращает user_id и role.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SERVICE)
    )
    @ApiResponse(responseCode = "200", description = "Токен проверен")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Токен недействителен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    VerifyTokenResponse verifyToken(@Valid @RequestBody VerifyTokenRequest request);

    @Operation(
            summary = "Проверить конкретное право",
            description = "Сервис-сервис операция. Проверяет, разрешено ли пользователю конкретное право, например checklist.approve_item.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SERVICE)
    )
    @ApiResponse(responseCode = "200", description = "Результат проверки права")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/permissions/check", consumes = MediaType.APPLICATION_JSON_VALUE)
    PermissionCheckResponse checkPermission(@Valid @RequestBody PermissionCheckRequest request);

    @Operation(
            summary = "Аутентификация по логину и паролю",
            description = "BFF проверяет данные в Keycloak. При успехе Keycloak генерирует OTP, BFF отправляет код пользователю и возвращает статус ожидания OTP.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "200", description = "Успешно, ожидайте OTP-код")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    LoginResponse login(@Valid @RequestBody LoginRequest request);

    @Operation(
            summary = "Проверить OTP-код",
            description = "BFF передаёт OTP в Keycloak. Где и совершается проверка.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(
            responseCode = "200",
            description = "OTP подтверждён. Установлена HttpOnly cookie сессии.",
            headers = @Header(name = "Set-Cookie", description = "HttpOnly cookie с идентификатором сессии")
    )
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Неверный или истёкший OTP-код",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/otp/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserProfileResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request);

    @Operation(
            summary = "Регистрация нового сотрудника",
            description = "Доступно только Супервайзеру и Админу.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "201", description = "Сотрудник зарегистрирован")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    ResponseEntity<UserProfileResponse> register(@Valid @RequestBody RegistrationRequest request);

    @Operation(
            summary = "Выход из системы",
            description = "BFF инвалидирует сессию, удаляет токены на своей стороне, очищает cookie у клиента и отзывает токены в Keycloak.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(
            responseCode = "204",
            description = "Сессия завершена, cookie очищена",
            headers = @Header(name = "Set-Cookie", description = "Очищенная/истёкшая cookie сессии")
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Void> logout();

    @Operation(
            summary = "Получить текущего пользователя",
            description = "По сессионной cookie BFF возвращает профиль пользователя и его роль для отрисовки интерфейса.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "200", description = "Профиль пользователя")
    @ApiResponse(responseCode = "401", description = "Сессия отсутствует или недействительна",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    UserProfileResponse getCurrentUser();

    @Operation(
            summary = "Заблокировать учётную запись",
            description = "Супервайзер или Администратор принудительно блокирует доступ сотрудника. BFF отправляет команду в Keycloak.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "204", description = "Учётная запись заблокирована")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/users/{id}/lock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    ResponseEntity<Void> lockUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable UUID id
    );

    @Operation(
            summary = "Разблокировать учётную запись",
            description = "Администратор или Супервайзер снимает блокировку учётной записи досрочно. BFF сбрасывает статус блокировки в Keycloak.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "204", description = "Учётная запись разблокирована")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/users/{id}/unlock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    ResponseEntity<Void> unlockUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable UUID id
    );
}