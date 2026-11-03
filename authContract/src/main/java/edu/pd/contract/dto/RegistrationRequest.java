package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на регистрацию нового сотрудника (доступно Супервайзеру и Админу)")
public record RegistrationRequest(

        @Schema(
                description = "Логин нового сотрудника",
                example = "ivan.petrov",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Логин обязателен")
        @Size(min = 3, max = 100, message = "Логин должен быть от 3 до 100 символов")
        String username,

        @Schema(
                description = "Начальный пароль сотрудника",
                example = "P@ssw0rd!",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, max = 128, message = "Пароль должен быть от 8 до 128 символов")
        String password,

        @Schema(
                description = "Имя сотрудника",
                example = "Иван",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Имя обязательно")
        @Size(max = 100, message = "Имя не должно превышать 100 символов")
        String firstName,

        @Schema(
                description = "Фамилия сотрудника",
                example = "Петров",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Фамилия обязательна")
        @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
        String lastName,

        @Schema(
                description = "Отчество сотрудника",
                example = "Петровович",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Size(max = 100, message = "Отчество не должна превышать 100 символов")
        String pavetry,

        @Schema(
                description = "Номер телефона для OTP (в международном формате)",
                example = "+79161234567",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Номер телефона обязателен")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Номер телефона должен содержать от 10 до 15 цифр и может начинаться со знака +"
        )
        String phone,

        @Schema(
                description = "Назначаемая роль",
                example = "EMPLOYEE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Роль обязательна")
        UserRole role
) {}