package edu.pd.authservice;

import edu.pd.contract.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice(assignableTypes = BffController.class)
public class BffExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException exception,
                                                     HttpServletRequest request) {
        return error(exception.getStatusCode().value(), exception.getReason(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                         HttpServletRequest request) {
        List<ErrorResponse.FieldError> fields = exception.getBindingResult().getFieldErrors().stream()
                // Do not echo passwords or OTP values in error responses.
                .map(field -> new ErrorResponse.FieldError(field.getField(), null, field.getDefaultMessage()))
                .toList();
        String detail = request.getRequestURI().endsWith("/otp/verify")
                ? "Неверный OTP-код" : "Ошибка валидации запроса";
        return error(HttpStatus.BAD_REQUEST.value(), detail, request, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpServletRequest request) {
        String detail = request.getRequestURI().endsWith("/otp/verify")
                ? "Неверный OTP-код: передайте JSON с полем otp" : "Некорректное тело запроса";
        return error(HttpStatus.BAD_REQUEST.value(), detail, request, null);
    }

    private ResponseEntity<ErrorResponse> error(int status, String detail, HttpServletRequest request,
                                                List<ErrorResponse.FieldError> fields) {
        return ResponseEntity.status(status).body(new ErrorResponse(status, "about:blank",
                HttpStatus.valueOf(status).getReasonPhrase(), detail,
                request.getRequestURI(), Instant.now(), fields));
    }
}
