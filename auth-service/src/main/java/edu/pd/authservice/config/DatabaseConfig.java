package edu.pd.authservice.config;

import edu.pd.authservice.entity.*;
import edu.pd.authservice.repository.*;
import edu.pd.contract.dto.UserRole;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Configuration
public class DatabaseConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnProperty(name = "auth.seed-demo-users", havingValue = "true")
    ApplicationRunner seedUsers(UserRepository users, RoleRepository roles,
                                PermissionRepository permissions, PasswordEncoder passwords,
                                TransactionTemplate transactions) {
        return args -> transactions.executeWithoutResult(status -> {
            Map<String, Permission> rights = new LinkedHashMap<>();
            for (String name : List.of("checklist:read", "checklist:create", "checklist:update", "checklist:archive")) {
                rights.put(name, permissions.findByName(name).orElseGet(() -> permissions.save(new Permission(name))));
            }
            Role admin = roles.findByName(UserRole.ADMIN).orElseGet(() ->
                    roles.save(new Role(UserRole.ADMIN, rights.values())));
            Role supervisor = roles.findByName(UserRole.SUPERVISOR).orElseGet(() ->
                    roles.save(new Role(UserRole.SUPERVISOR, List.of(rights.get("checklist:read"),
                            rights.get("checklist:create"), rights.get("checklist:update")))));
            Role technician = roles.findByName(UserRole.TECHNICIAN).orElseGet(() ->
                    roles.save(new Role(UserRole.TECHNICIAN, List.of(rights.get("checklist:read"), rights.get("checklist:update")))));
            Map<String, Role> accounts = Map.of("admin", admin, "supervisor", supervisor,
                    "technician", technician, "editor", supervisor, "student", technician);
            accounts.forEach((name, role) -> {
                if (users.findByUsername(name).isEmpty()) users.save(new AppUser(name, passwords.encode(name), role));
            });
        });
    }
}
