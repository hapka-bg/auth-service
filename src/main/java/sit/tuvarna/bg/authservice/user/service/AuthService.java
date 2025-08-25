package sit.tuvarna.bg.authservice.user.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.authservice.blacklistedToken.model.BlacklistedToken;
import sit.tuvarna.bg.authservice.blacklistedToken.repository.BlacklistedTokenRepository;
import sit.tuvarna.bg.authservice.user.model.Role;
import sit.tuvarna.bg.authservice.user.model.User;
import sit.tuvarna.bg.authservice.user.repository.UserRepository;
import sit.tuvarna.bg.authservice.utils.JwtUtil;
import sit.tuvarna.bg.authservice.utils.TurnstileService;
import sit.tuvarna.bg.authservice.web.dto.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final TurnstileService turnstile;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    public AuthService(UserRepository repo, PasswordEncoder encoder, JwtUtil jwt, TurnstileService turnstile, BlacklistedTokenRepository blacklistedTokenRepository) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.turnstile = turnstile;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    public AuthResponse register(RegisterRequest r) {
//        if (!turnstile.verify(r.turnstileToken())) throw new RuntimeException("CAPTCHA fail");
        if (repo.existsByEmail(r.email())) throw new RuntimeException("Email exists");
        User u = User.builder()
                .email(r.email())
                .password(encoder.encode(r.password()))
                .firstName(r.firstName())
                .lastName(r.lastName())
                .phoneNumber(r.phone())
                .role(Role.USER)
                .city(r.city())
                .street(r.street())
                .build();
        repo.save(u);
        return tokens(u);
    }

    public AuthResponse login(LoginRequest r) {
        //todo validate with username
        User u = repo.findByEmail(r.email()).orElseThrow();
        if (!encoder.matches(r.password(), u.getPassword())) throw new RuntimeException("Bad creds");
        return tokens(u);
    }

    public String refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwt.parseToken(refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        String jti = jwt.extractJti(refreshToken);
        if (blacklistedTokenRepository.existsByJti(jti)) {
            throw new RuntimeException("Refresh token is blacklisted. Please log in again.");
            //todo somehow redirect to login page
        }
        User u = repo.findByEmail(claims.getSubject()).orElseThrow();
        return jwt.generateAccessToken(u);
    }

    public ResponseCookie buildCookieForRefreshToken(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    private AuthResponse tokens(User u) {
        return new AuthResponse(jwt.generateAccessToken(u), jwt.generateRefreshToken(u));
    }

    public ResponseCookie logout(String refreshToken) {
        if (refreshToken != null) {
            String jti = jwt.extractJti(refreshToken);
            Instant instant = jwt.extractExp(refreshToken).toInstant();
            BlacklistedToken build = BlacklistedToken.builder()
                    .jti(jti)
                    .expiresAt(instant)
                    .build();

            blacklistedTokenRepository.save(build);
        }
        return ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .maxAge(0)
                .build();
    }


    public List<AllUsersResponse> getAllUsers() {
        List<User> all = repo.findAll();
        List<AllUsersResponse> list = new ArrayList<>();
        for (User user : all) {
            AllUsersResponse build = AllUsersResponse.builder()
                    .email(user.getEmail())
                    .salary(user.getStaffDetail().getSalary().toPlainString())
                    .name(user.getFirstName() + " " + user.getLastName())
                    .role(String.valueOf(user.getRole()))
                    .build();
            list.add(build);
        }
        System.out.println("Successfully iterated through all users");
        return list;
    }

    public List<String> getNames(List<UUID> ids) {
        System.out.println(ids);
        List<User> users = repo.findAllById(ids);
        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return ids.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .toList();
    }

    public UserDetailsForOnlineOrders getPhoneNumberAndAddress(UUID id) {
        User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
      return  UserDetailsForOnlineOrders.builder()
                .address(user.getStreet())
                .phoneNumber(user.getPhoneNumber())
                .build();

    }
}