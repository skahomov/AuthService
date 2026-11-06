package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роль пользователя в системе")
public enum UserRole {
    ADMIN,

    @Schema(description = "Супервайзер")
    SUPERVISOR,

    @Schema(description = "Техник")
    TECHNICIAN
}