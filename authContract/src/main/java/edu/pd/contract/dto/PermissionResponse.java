package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Информация о праве")
public record PermissionResponse(

        @Schema(description = "Идентификатор права", example = "3f4a1b2c-5d6e-7f80-9a1b-2c3d4e5f6a7b")
        UUID id,

        @Schema(description = "Название права", example = "checklist.approve_item")
        String name
) {}