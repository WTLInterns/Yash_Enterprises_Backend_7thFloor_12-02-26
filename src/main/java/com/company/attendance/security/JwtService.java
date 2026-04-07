package com.company.attendance.security;

import org.springframework.stereotype.Component;

@Component
public class JwtService {

    public String generateToken(String email, String role) {
        // JWT removed — return empty string
        return "";
    }

    public String extractEmail(String token) {
        return null;
    }

    public boolean isTokenValid(String token) {
        return false;
    }
}
