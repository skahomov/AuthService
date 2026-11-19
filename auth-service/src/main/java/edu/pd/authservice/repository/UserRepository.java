package edu.pd.authservice.repository;

import edu.pd.authservice.entity.AppUser;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface UserRepository extends JpaRepository<AppUser, UUID> {
    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<AppUser> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<AppUser> findById(UUID id);
}
