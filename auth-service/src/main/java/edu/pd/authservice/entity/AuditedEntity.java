package edu.pd.authservice.entity;

import jakarta.persistence.*;
import java.time.*;

@MappedSuperclass
public abstract class AuditedEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(Clock.systemUTC()); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(Clock.systemUTC()); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
