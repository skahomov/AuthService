package edu.pd.authservice;

import edu.pd.contract.dto.*;
import edu.pd.contract.endpoints.AuthApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class BffController implements AuthApi {
    private static final String VALID_OTP = "111111";
    private static final String PENDING_USER = "pendingUser";
    private final ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
    private final HttpServletRequest servletRequest;

    public BffController(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
        users.put("editor", "editor");
        users.put("student", "student");
        users.put("admin", "admin");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        HttpSession previous = servletRequest.getSession(false);
        if (previous != null) {
            previous.invalidate();
        }
        if (request == null || request.username() == null || request.password() == null
                || !request.password().equals(users.get(request.username()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }
        servletRequest.getSession(true).setAttribute(PENDING_USER, request.username());
        return new LoginResponse("OTP_REQUIRED", "Введите OTP-код", null);
    }

    @Override
    public ResponseEntity<UserProfileResponse> verifyOtp(OtpVerifyRequest request) {
        if (request == null || request.otp() == null || request.otp().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный OTP-код");
        }
        HttpSession session = servletRequest.getSession(false);
        String username = session == null ? null : (String) session.getAttribute(PENDING_USER);
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сначала выполните login");
        }
        if (!VALID_OTP.equals(request.otp())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный OTP-код");
        }
        session.removeAttribute(PENDING_USER);
        UserRole role;
        switch (username) {
            case "admin" -> role = UserRole.ADMIN;
            case "supervisor" -> role = UserRole.SUPERVISOR;
            case "technician" -> role = UserRole.TECHNICIAN;
            default -> role = null;
        }
        return ResponseEntity.ok(new UserProfileResponse(
                UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8)),
                username, null, null, null, role));
    }

    @Override
    public VerifyTokenResponse verifyToken(VerifyTokenRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PermissionCheckResponse checkPermission(PermissionCheckRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ResponseEntity<UserProfileResponse> register(RegistrationRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ResponseEntity<Void> logout() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public UserProfileResponse getCurrentUser() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ResponseEntity<Void> lockUser(UUID id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ResponseEntity<Void> unlockUser(UUID id) {
        throw new UnsupportedOperationException("TODO");
    }
}
