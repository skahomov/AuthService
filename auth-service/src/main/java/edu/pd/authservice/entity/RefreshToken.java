package edu.pd.authservice.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private AppUser user;
    @Column(name = "token_hash", nullable = false, length = 255) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
    @Column(nullable = false) private boolean revoked;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    protected RefreshToken() {}
    public RefreshToken(AppUser user, String tokenHash, LocalDateTime expiresAt) {
        id = UUID.randomUUID(); this.user = user; this.tokenHash = tokenHash; this.expiresAt = expiresAt;
        createdAt = LocalDateTime.now(Clock.systemUTC());
    }
    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void revoke() { revoked = true; }
}
