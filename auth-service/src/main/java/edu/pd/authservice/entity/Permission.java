package edu.pd.authservice.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "permissions")
public class Permission extends AuditedEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String name;

    protected Permission() {
    }

    public Permission(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
