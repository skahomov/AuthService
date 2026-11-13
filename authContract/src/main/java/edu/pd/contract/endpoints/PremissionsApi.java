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
import edu.pd.contract.dto.PermissionCreateRequest;
import edu.pd.contract.dto.PermissionResponse;
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
        name = "Auth-permissions",
        description = "Сервис авторизации: API управления ролями, правами и пользователями"
)
@RequestMapping(
        value = "/api/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface PremissionsApi {

    String SECURITY_SCHEME_SERVICE = "serviceBearer";
    String SECURITY_SCHEME_SESSION = "sessionCookie";

    @Operation(
            summary = "Создать право",
            description = "Создает новое право в системе.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "201", description = "Право создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/permits/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody PermissionCreateRequest request);

    @Operation(
            summary = "Удалить право",
            description = "Удаляет право. Ограничение: нельзя удалить право, если оно связано с ролью.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_SESSION)
    )
    @ApiResponse(responseCode = "204", description = "Право удалено")
    @ApiResponse(responseCode = "400", description = "Право используется",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Право не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/permits/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> deletePermission(
            @Parameter(description = "ID права", required = true) @PathVariable UUID id
    );
}