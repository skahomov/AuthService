package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на проверку JWT-токена (сервис-сервис)")
public record VerifyTokenRequest(

        @Schema(
                description = "JWT-токен, переданный другим сервисом для проверки",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "JWT токен обязателен")
        @Size(max = 4096, message = "JWT токен слишком длинный")
        String token
) {}