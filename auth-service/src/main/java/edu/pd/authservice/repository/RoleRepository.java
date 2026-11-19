package edu.pd.authservice.repository;

import edu.pd.authservice.entity.Role;
import edu.pd.contract.dto.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(UserRole name);
}
