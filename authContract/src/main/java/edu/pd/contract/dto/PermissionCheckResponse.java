package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат проверки права")
public record PermissionCheckResponse(

        @Schema(description = "Разрешено ли выполнение действия", example = "true")
        boolean allowed
) {}