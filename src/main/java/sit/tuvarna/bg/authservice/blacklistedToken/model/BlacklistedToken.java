package sit.tuvarna.bg.authservice.blacklistedToken.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "blacklisted_tokens")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BlacklistedToken {

    @Id
    private String jti;
    private Instant expiresAt;

}
