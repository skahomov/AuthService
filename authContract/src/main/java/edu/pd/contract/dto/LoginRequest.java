package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на аутентификацию по логину и паролю")
public record LoginRequest(

        @Schema(
                description = "Логин пользователя",
                example = "ivan.petrov",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Логин обязателен")
        @Size(max = 100, message = "Логин не должен превышать 100 символов")
        String username,

        @Schema(
                description = "Пароль пользователя",
                example = "P@ssw0rd!",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Пароль обязателен")
        @Size(min = 1, max = 128, message = "Пароль должен быть от 1 до 128 символов")
        String password
) {}