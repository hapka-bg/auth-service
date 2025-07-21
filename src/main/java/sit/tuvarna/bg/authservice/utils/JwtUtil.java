package sit.tuvarna.bg.authservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.tuvarna.bg.authservice.user.model.User;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.access-exp-ms}")
    private long accessExp;
    @Value("${jwt.refresh-exp-ms}")
    private long refreshExp;


    public String generateAccessToken(User user) {
        return buildToken(user,accessExp);
    }
    public String generateRefreshToken(User user) {
        return buildToken(user,refreshExp);
    }
    public String extractJti(String token) {
        return parseToken(token).getId();
    }

    public Date extractExp(String token) {
        return parseToken(token).getExpiration();
    }

    private String buildToken(User user, long exp) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getEmail())
                .claim("role",user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+exp))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

