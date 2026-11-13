package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание нового права")
public record PermissionCreateRequest(

        @Schema(
                description = "Название права, например checklist.approve_item",
                example = "checklist.approve_item",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Название права обязательно")
        @Size(max = 255, message = "Название права не должно превышать 255 символов")
        String name
) {}