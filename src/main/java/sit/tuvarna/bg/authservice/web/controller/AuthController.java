package sit.tuvarna.bg.authservice.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.authservice.user.service.AuthService;
import sit.tuvarna.bg.authservice.web.dto.AuthResponse;
import sit.tuvarna.bg.authservice.web.dto.LoginRequest;
import sit.tuvarna.bg.authservice.web.dto.RegisterRequest;

import java.util.Map;

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
}
