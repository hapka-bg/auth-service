package sit.tuvarna.bg.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.tuvarna.bg.authservice.user.model.User;
import sit.tuvarna.bg.authservice.user.repository.UserRepository;
import sit.tuvarna.bg.authservice.utils.JwtUtil;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String h=request.getHeader("Authorization");
        if(h!=null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try{
                Claims claims= jwtUtil.parseToken(token);
                String email = claims.getSubject();
                User user = userRepository.findByEmail(email).orElse(null);
                if(user!=null){
                    var authorities=List.of(new SimpleGrantedAuthority("ROLE_" +user.getRole().name()));
                    var authToken=   new UsernamePasswordAuthenticationToken(user,null,authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }catch (JwtException e){
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/api/auth/refresh");
    }
}
