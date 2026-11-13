package edu.pd.contract.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.UUID;

import edu.pd.contract.dto.ErrorResponse;
import edu.pd.contract.dto.RoleCreateRequest;
import edu.pd.contract.dto.RolePermissionsRequest;
import edu.pd.contract.dto.RoleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(
        name = "Auth-roles",
        description = "Сервис авторизации: API управления ролями, правами и пользователями"
)
@RequestMapping(
        value = "/api/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface RoleApi {

    String SECURITY_SCHEME_SERVICE = "serviceBearer";
    String SECURITY_SCHEME_SESSION = "sessionCookie";

    @Operation(
            summary = "Создать роль",
            description = "Создает новую роль в системе.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "201", description = "Роль создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/role/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request);

    @Operation(
            summary = "Удалить роль",
            description = "Удаляет роль. Ограничение: нельзя удалить роль, если с ней связан пользователь.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "204", description = "Роль удалена")
    @ApiResponse(responseCode = "400", description = "Роль используется пользователем",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Роль не найдена",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/role/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> deleteRole(
            @Parameter(description = "ID роли", required = true) @PathVariable UUID id
    );

    @Operation(
            summary = "Назначить права роли",
            description = "Назначает список прав указанной роли.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "200", description = "Права успешно назначены")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/role/{id}/set-permits", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> setRolePermissions(
            @Parameter(description = "ID роли", required = true) @PathVariable UUID id,
            @Valid @RequestBody RolePermissionsRequest request
    );

    @Operation(
            summary = "Отозвать права у роли",
            description = "Отзывает список прав у указанной роли.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "200", description = "Права успешно отозваны")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/role/{id}/revoke-permits", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> revokeRolePermissions(
            @Parameter(description = "ID роли", required = true) @PathVariable UUID id,
            @Valid @RequestBody RolePermissionsRequest request
    );
}