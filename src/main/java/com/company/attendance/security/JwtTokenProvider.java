package com.company.attendance.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public String generateToken(String subject) {
        return "";
    }

    public String getSubject(String token) {
        return null;
    }

    public boolean validateToken(String token) {
        return false;
    }
}
