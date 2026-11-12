package edu.pd.contract.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

import edu.pd.contract.dto.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(
        name = "Auth-users",
        description = "Сервис авторизации: API управления ролями, правами и пользователями"
)
@RequestMapping(
        value = "/api/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface UserApi {

    String SECURITY_SCHEME_SERVICE = "serviceBearer";
    String SECURITY_SCHEME_SESSION = "sessionCookie";

    @Operation(
            summary = "Назначить роль пользователю",
            description = "Назначает роль указанному пользователю.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "200", description = "Роль успешно назначена")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/users/{id}/set-role/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    ResponseEntity<Void> setUserRole(
            @Parameter(description = "ID пользователя", required = true) @PathVariable UUID id,
            @Parameter(description = "ID роли", required = true) @PathVariable UUID roleId
    );

    @Operation(
            summary = "Отозвать роль у пользователя",
            description = "Отзывает роль у указанного пользователя.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "200", description = "Роль успешно отозвана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/users/{id}/revoke-role/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    ResponseEntity<Void> revokeUserRole(
            @Parameter(description = "ID пользователя", required = true) @PathVariable UUID id,
            @Parameter(description = "ID роли", required = true) @PathVariable UUID roleId
    );
}