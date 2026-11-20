package edu.pd.authservice.repository;

import edu.pd.authservice.entity.OtpCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OtpCode o where o.id = :id")
    Optional<OtpCode> findForUpdate(@Param("id") UUID id);
}
