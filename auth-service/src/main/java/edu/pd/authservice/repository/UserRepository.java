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
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AppUser u where u.username = :username")
    Optional<AppUser> findForUpdateByUsername(@org.springframework.data.repository.query.Param("username") String username);
}
