package edu.pd.authservice.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "auth_users")
public class AppUser {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(length = 32)
    private String phone;
    @Column(nullable = false)
    private boolean enabled = true;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, Role role) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Role getRole() {
        return role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
