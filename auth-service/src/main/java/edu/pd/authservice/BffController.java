package edu.pd.authservice;

import edu.pd.contract.dto.*;
import edu.pd.contract.endpoints.AuthApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class BffController implements AuthApi {
    private static final String PENDING_USER = "pendingUserId";
    private static final String PENDING_CODE = "pendingOtpId";
    private final edu.pd.authservice.service.AuthService auth;
    private final HttpServletRequest servletRequest;

    public BffController(HttpServletRequest servletRequest, edu.pd.authservice.service.AuthService auth) {
        this.servletRequest = servletRequest;
        this.auth = auth;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        HttpSession previous = servletRequest.getSession(false);
        if (previous != null) previous.invalidate();
        var challenge = auth.login(request);
        HttpSession session = servletRequest.getSession(true);
        session.setMaxInactiveInterval(300);
        session.setAttribute(PENDING_USER, challenge.userId());
        session.setAttribute(PENDING_CODE, challenge.codeId());
        return new LoginResponse("OTP_REQUIRED", "Введите OTP-код", null);
    }

    @Override
    public ResponseEntity<UserProfileResponse> verifyOtp(OtpVerifyRequest request) {
        HttpSession session = servletRequest.getSession(false);
        if (session == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сначала выполните login");
        // Serialize concurrent attempts: a successful pending login can issue only one token.
        synchronized (session) {
            UUID userId = (UUID) session.getAttribute(PENDING_USER);
            UUID codeId = (UUID) session.getAttribute(PENDING_CODE);
            if (userId == null || codeId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сначала выполните login");
            var result = auth.verifyOtp(userId, codeId, request.otp());
            session.removeAttribute(PENDING_USER);
            session.removeAttribute(PENDING_CODE);
            return ResponseEntity.ok().header("Authorization", "Bearer " + result.token())
                    .header("Cache-Control", "no-store").header("Pragma", "no-cache").body(result.profile());
        }
    }

    @Override
    public VerifyTokenResponse verifyToken(VerifyTokenRequest request) {
        return auth.verifyToken(request.token());
    }

    @Override
    public PermissionCheckResponse checkPermission(PermissionCheckRequest request) {
        return auth.checkPermission(request);
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
