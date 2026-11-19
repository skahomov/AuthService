package edu.pd.authservice.service;

import edu.pd.authservice.entity.AppUser;
import edu.pd.authservice.repository.UserRepository;
import edu.pd.contract.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwords, TokenService tokens) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    public UUID login(LoginRequest request) {
        AppUser user = users.findByUsername(request.username()).filter(AppUser::isEnabled)
                .orElseThrow(() -> unauthorized("Неверный логин или пароль"));
        if (!passwords.matches(request.password(), user.getPasswordHash()))
            throw unauthorized("Неверный логин или пароль");
        return user.getId();
    }

    public VerifiedLogin verifyOtp(UUID userId, String otp) {
        if (!"111111".equals(otp)) throw unauthorized("Неверный OTP-код");
        AppUser user = enabledUser(userId);
        return new VerifiedLogin(new UserProfileResponse(user.getId(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getPhone(), user.getRole().getName()), tokens.issue(user));
    }

    public VerifyTokenResponse verifyToken(String token) {
        try {
            var jwt = tokens.decode(token);
            String id = jwt.getClaimAsString("user_id");
            AppUser user = enabledUser(UUID.fromString(id == null ? jwt.getSubject() : id));
            return new VerifyTokenResponse(user.getId(), user.getRole().getName());
        } catch (JwtException | IllegalArgumentException exception) {
            throw unauthorized("Недействительный JWT-токен");
        }
    }

    public PermissionCheckResponse checkPermission(PermissionCheckRequest request) {
        boolean allowed = users.findById(request.userId()).filter(AppUser::isEnabled)
                .map(user -> user.getRole().getName() == UserRole.ADMIN
                        || user.getRole().getPermissions().stream().anyMatch(p -> p.getName().equals(request.permission())))
                .orElse(false);
        return new PermissionCheckResponse(allowed);
    }

    private AppUser enabledUser(UUID id) {
        return users.findById(id).filter(AppUser::isEnabled)
                .orElseThrow(() -> unauthorized("Пользователь не найден или заблокирован"));
    }

    private ResponseStatusException unauthorized(String reason) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
    }

    public record VerifiedLogin(UserProfileResponse profile, String token) {
    }
}
