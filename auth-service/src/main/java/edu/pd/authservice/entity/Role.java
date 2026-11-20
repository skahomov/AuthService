package edu.pd.authservice.entity;

import edu.pd.contract.dto.UserRole;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "roles")
public class Role extends AuditedEntity {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private UserRole name;
    @ManyToMany
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    protected Role() {
    }

    public Role(UserRole name, Collection<Permission> permissions) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.permissions.addAll(permissions);
    }

    public UUID getId() {
        return id;
    }

    public UserRole getName() {
        return name;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}
