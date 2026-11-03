package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Профиль авторизованного пользователя")
public record UserProfileResponse(

        @Schema(description = "Идентификатор пользователя", example = "3f4a1b2c-5d6e-7f80-9a1b-2c3d4e5f6a7b")
        UUID id,

        @Schema(description = "Логин", example = "ivan.petrov")
        String username,

        @Schema(description = "Имя", example = "Иван")
        String firstName,

        @Schema(description = "Фамилия", example = "Петров")
        String lastName,

        @Schema(description = "Номер телефона", example = "+79161234567")
        String phone,

        @Schema(description = "Роль пользователя", example = "TECHNIСIAN")
        UserRole role
) {}