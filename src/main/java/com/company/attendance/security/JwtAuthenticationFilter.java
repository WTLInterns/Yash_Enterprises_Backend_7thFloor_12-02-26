package com.company.attendance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// @Component - DISABLED FOR LIFETIME
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip JWT processing for public endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/ws/") || path.startsWith("/topic/") || path.startsWith("/api/debug/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check for Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // Validate JWT format before processing
        if (jwt.isEmpty() || jwt.split("\\.").length != 3) {
            log.warn("Invalid JWT format: {}", jwt.isEmpty() ? "empty" : "malformed");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract claims from token (NO DATABASE ACCESS)
            Claims claims = jwtService.extractAllClaims(jwt);
            userEmail = claims.getSubject();
            String role = claims.get("role", String.class);

            if (userEmail != null && role != null) {
                // Build authentication directly from token claims (NO DB)
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase().trim())
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail, null, authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Set authentication for user: {} with role: {}", userEmail, role);
            }
        } catch (JwtException e) {
            log.warn("JWT processing failed: {}", e.getMessage());
            // Continue without authentication
        } catch (Exception e) {
            log.warn("Unexpected error during JWT processing: {}", e.getMessage());
            // Continue without authentication
        }

        filterChain.doFilter(request, response);
    }
}
