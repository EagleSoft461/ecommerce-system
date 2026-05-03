package com.ecommerce.product.config;

import com.ecommerce.product.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // API Gateway'den gelen X-User-Email header'ını kontrol et
        String userEmail = request.getHeader("X-User-Email");

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // DB'ye gitmeden, sadece header'dan kullanıcıyı tanı
            // Role bilgisi için Authorization header'ından token'ı parse et
            String authHeader = request.getHeader("Authorization");
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String role = jwtUtil.extractRole(token);
                    if (role != null) {
                        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                } catch (Exception e) {
                    log.warn("Could not extract role from token: {}", e.getMessage());
                }
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail, null, authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
