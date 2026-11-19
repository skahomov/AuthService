package edu.pd.authservice.repository;

import edu.pd.authservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
}
