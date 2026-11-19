package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на назначение или отзыв прав у роли")
public record RolePermissionsRequest(

        @Schema(
                description = "Список ID прав",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Список прав не может быть пустым")
        List<UUID> permissionIds
) {}