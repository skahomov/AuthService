package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на проверку одноразового OTP-кода")
public record OtpVerifyRequest(

        @Schema(
                description = "Одноразовый OTP-код",
                example = "111111",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Неверный OTP-код")
        @Pattern(regexp = "^\\d{6}$", message = "Неверный OTP-код: ожидаются 6 цифр")
        String otp
) {}