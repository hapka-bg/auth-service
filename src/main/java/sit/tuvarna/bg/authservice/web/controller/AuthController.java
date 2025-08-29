package sit.tuvarna.bg.authservice.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.authservice.user.service.AuthService;
import sit.tuvarna.bg.authservice.web.dto.*;
import sit.tuvarna.bg.authservice.web.dto.updatingUser.ChangePasswordRequest;
import sit.tuvarna.bg.authservice.web.dto.updatingUser.UpdateResponse;
import sit.tuvarna.bg.authservice.web.dto.updatingUser.UserDetailsDto;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<?> reg(@RequestBody RegisterRequest r){
        AuthResponse register = auth.register(r);
        ResponseCookie refreshCookie  = auth.buildCookieForRefreshToken(register.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie .toString())
                .body(Map.of("accessToken",register.accessToken()));
    }
    @PostMapping("/login")
    public ResponseEntity<?> log(@RequestBody LoginRequest r){
        AuthResponse login = auth.login(r);
        ResponseCookie refreshCookie = auth.buildCookieForRefreshToken(login.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("accessToken", login.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken){
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token");
        }
        String newAccessToken  = auth.refresh(refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken",required = false) String refreshToken){
        ResponseCookie logout = auth.logout(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,logout.toString())
                .body(Map.of("message","Logged out successfully"));
    }

    @GetMapping("/extract-user-id")
    public ResponseEntity<UUID> extractUserId(@RequestHeader("Authorization") String authHeader){
        UUID uuid = auth.extractUserId(authHeader);
        return ResponseEntity.ok(uuid);
    }
    @GetMapping("/user-details")
    public ResponseEntity<UserProfileDetails>  userDetails(@RequestHeader("Authorization") String authHeader){
        UserProfileDetails userDetails = auth.getUserDetails(authHeader);
        return ResponseEntity.ok(userDetails);
    }

    @PutMapping("/user-details")
    public ResponseEntity<UpdateResponse> updateUser(@RequestBody UserDetailsDto updatedDetails,
                                                     @RequestHeader("Authorization") String authHeader){
        UpdateResponse updateResponse = auth.updateUser(updatedDetails, authHeader);
        return ResponseEntity.ok(updateResponse);
    }
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,@RequestHeader("Authorization") String authHeader){
        auth.changePassword(request,authHeader);
        return ResponseEntity.ok("Password updated successfully");

    }
}
