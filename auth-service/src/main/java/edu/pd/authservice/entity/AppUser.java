package edu.pd.authservice.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class AppUser extends AuditedEntity {
    @Id private UUID id;
    @Column(name = "login", nullable = false, unique = true, length = 100)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    @Column(nullable = false, unique = true, length = 20)
    private String phone;
    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    protected AppUser() {}
    public AppUser(String username, String passwordHash, String fullName, String phone, Role role) {
        id = UUID.randomUUID(); this.username = username; this.passwordHash = passwordHash;
        this.fullName = fullName; this.phone = phone; this.role = role;
    }
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    // Preserve the existing response contract while the database stores a single full_name.
    public String getFirstName() { return fullName.strip().split("\\s+", 2)[0]; }
    public String getLastName() {
        String[] parts = fullName.strip().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public boolean isEnabled() {
        return "ACTIVE".equals(status) && (blockedUntil == null || !blockedUntil.isAfter(LocalDateTime.now(Clock.systemUTC())));
    }
    public Role getRole() { return role; }
    public void setStatus(String status) { this.status = status; }
    public void setEnabled(boolean enabled) { status = enabled ? "ACTIVE" : "DISABLED"; }
    public void setBlockedUntil(LocalDateTime time) { blockedUntil = time; }
    public void recordFailedLogin() { failedLoginAttempts++; }
    public void resetFailedLogins() { failedLoginAttempts = 0; }
}
