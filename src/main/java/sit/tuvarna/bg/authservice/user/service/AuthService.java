package sit.tuvarna.bg.authservice.user.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.authservice.blacklistedToken.model.BlacklistedToken;
import sit.tuvarna.bg.authservice.blacklistedToken.repository.BlacklistedTokenRepository;
import sit.tuvarna.bg.authservice.staffDetail.model.StaffDetail;
import sit.tuvarna.bg.authservice.user.model.Role;
import sit.tuvarna.bg.authservice.user.model.User;
import sit.tuvarna.bg.authservice.user.repository.UserRepository;
import sit.tuvarna.bg.authservice.utils.JwtUtil;
import sit.tuvarna.bg.authservice.utils.TurnstileService;
import sit.tuvarna.bg.authservice.web.dto.*;
import sit.tuvarna.bg.authservice.web.dto.updatingUser.ChangePasswordRequest;
import sit.tuvarna.bg.authservice.web.dto.updatingUser.UpdateResponse;
import sit.tuvarna.bg.authservice.web.dto.updatingUser.UserDetailsDto;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
        if (!turnstile.verify(r.turnstileToken())) throw new RuntimeException("CAPTCHA fail");
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
        return UserDetailsForOnlineOrders.builder()
                .address(user.getStreet())
                .phoneNumber(user.getPhoneNumber())
                .build();

    }

    public void addStaff(AddStaffRequest addStaff) {
        if (repo.existsByEmail(addStaff.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }
        User build = User.builder()
                .firstName(addStaff.getFirstName())
                .lastName(addStaff.getLastName())
                .email(addStaff.getEmail())
                .phoneNumber(addStaff.getPhone())
                .role(Role.valueOf(addStaff.getRole().toUpperCase()))
                .build();

        StaffDetail staffDetail = StaffDetail.builder()
                .active(true)
                .hiredDate(LocalDate.now())
                .salary(addStaff.getSalary())
                .notes("empty")
                .build();


        staffDetail.setUser(build);
        build.setStaffDetail(staffDetail);
        repo.save(build);
    }

    public UUID extractUserId(String authHeader) {
        String email = jwt.extractEmailFromToken(authHeader);
        return repo.findByEmail(email)
                .map(User::getId).
                orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserProfileDetails getUserDetails(String authHeader) {
        String email = jwt.extractEmailFromToken(authHeader);
        User user = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return UserProfileDetails.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .address(user.getStreet())
                .city(user.getCity())
                .build();
    }

    public UpdateResponse updateUser(UserDetailsDto updatedDetails, String authHeader) {

        String email = jwt.extractEmailFromToken(authHeader);
        User user = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (updatedDetails.getEmail() != null &&
                !updatedDetails.getEmail().equals(user.getEmail())) {
            user.setEmail(updatedDetails.getEmail());
            repo.save(user);

            // Generate a new token with the new email
            String newToken = jwt.generateAccessToken(user);

            return new UpdateResponse(newToken, user);
        } else {
            // Update other fields if needed
            if (updatedDetails.getFirstName() != null)
                user.setFirstName(updatedDetails.getFirstName());
            if (updatedDetails.getLastName() != null) user.setLastName(updatedDetails.getLastName());
            if (updatedDetails.getPhone() != null) user.setPhoneNumber(updatedDetails.getPhone());
            if (updatedDetails.getAddress() != null) user.setStreet(updatedDetails.getAddress());
            if (updatedDetails.getCity() != null) user.setCity(updatedDetails.getCity());
            repo.save(user);

            return new UpdateResponse(authHeader,user);
        }

    }

    public void changePassword(ChangePasswordRequest request, String authHeader) {
        if(request.getOldPassword().isBlank() ||  request.getNewPassword().isBlank() || request.getConfirmNewPassword().isBlank()) throw new    RuntimeException("Old password and new password are empty");
        if(!request.getNewPassword().equals(request.getConfirmNewPassword())) throw new RuntimeException("New passwords do not match");

        String email = jwt.extractEmailFromToken(authHeader);
        User user = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if(!encoder.matches(request.getOldPassword(), user.getPassword())) throw new RuntimeException("Old passwords do not match");

        user.setPassword(encoder.encode(request.getNewPassword()));
        repo.save(user);

    }
}