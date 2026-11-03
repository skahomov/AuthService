package edu.pd.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на успешную аутентификацию: OTP-код отправлен пользователю")
public record LoginResponse(

        @Schema(description = "Статус операции", example = "OTP_REQUIRED")
        String status,

        @Schema(description = "Сообщение для пользователя", example = "Успешно, ожидайте OTP-код")
        String message,

        @Schema(description = "Замаскированный номер телефона, на который отправлен код", example = "+7***123")
        String maskedPhone
) {}