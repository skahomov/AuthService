package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роль пользователя в системе")
public enum UserRole {

    @Schema(description = "Администратор")
    ADMIN,

    @Schema(description = "Супервайзер")
    SUPERVISOR,

    @Schema(description = "Техник")
    TECHNIСIAN
}