package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание новой роли")
public record RoleCreateRequest(

        @Schema(
                description = "Название роли",
                example = "MANAGER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Название роли обязательно")
        @Size(max = 100, message = "Название роли не должно превышать 100 символов")
        String name
) {}