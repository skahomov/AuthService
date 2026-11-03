package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на проверку одноразового OTP-кода")
public record OtpVerifyRequest(

        @Schema(
                description = "Одноразовый OTP-код",
                example = "123456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "OTP-код обязателен")
        @Pattern(regexp = "^\\d{4,8}$", message = "OTP-код должен состоять из 4–8 цифр")
        String otp
) {}