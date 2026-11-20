package edu.pd.authservice.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "otp_codes")
public class OtpCode {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private AppUser user;
    @Column(name = "code_hash", nullable = false, length = 255) private String codeHash;
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
    @Column(nullable = false) private int attempts;
    @Column(nullable = false) private boolean used;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    protected OtpCode() {}
    public OtpCode(AppUser user, String codeHash, LocalDateTime expiresAt) {
        id = UUID.randomUUID(); this.user = user; this.codeHash = codeHash; this.expiresAt = expiresAt;
        createdAt = LocalDateTime.now(Clock.systemUTC());
    }
    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getCodeHash() { return codeHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getAttempts() { return attempts; }
    public boolean isUsed() { return used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void recordAttempt() { attempts++; }
    public void markUsed() { used = true; }
}
