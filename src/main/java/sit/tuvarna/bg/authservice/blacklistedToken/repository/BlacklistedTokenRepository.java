package sit.tuvarna.bg.authservice.blacklistedToken.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.tuvarna.bg.authservice.blacklistedToken.model.BlacklistedToken;

import java.time.Instant;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(Instant now);
}
