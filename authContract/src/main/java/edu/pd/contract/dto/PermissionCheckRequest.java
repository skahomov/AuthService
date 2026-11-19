package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Запрос на проверку конкретного права пользователя (сервис-сервис)")
public record PermissionCheckRequest(

        @Schema(
                description = "Идентификатор пользователя",
                example = "3f4a1b2c-5d6e-7f80-9a1b-2c3d4e5f6a7b",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Идентификатор пользователя обязателен")
        UUID userId,

        @Schema(
                description = "Проверяемое право, например checklist:update",
                example = "checklist:update",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Наименование права обязательно")
        @Size(max = 255, message = "Наименование права не должно превышать 255 символов")
        String permission
) {}