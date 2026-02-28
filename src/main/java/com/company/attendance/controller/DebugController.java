package com.company.attendance.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {
    
    @PostMapping("/test-login")
    public String testLogin(@RequestBody String loginRequest) {
        return "Received: " + loginRequest;
    }
    
    @GetMapping("/test-user")
    public String testUser() {
        return "Test user endpoint working";
    }
    
    @GetMapping("/role")
    public Map<String, Object> debugRole(Authentication authentication) {
        Map<String, Object> debug = new HashMap<>();
        
        if (authentication == null) {
            debug.put("authenticated", false);
            debug.put("message", "No authentication found");
            return debug;
        }
        
        debug.put("authenticated", true);
        debug.put("name", authentication.getName());
        debug.put("authorities", authentication.getAuthorities());
        debug.put("principal", authentication.getPrincipal().getClass().getSimpleName());
        debug.put("details", authentication.getDetails());
        
        // Check if ROLE_ prefix is present
        boolean hasRolePrefix = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().startsWith("ROLE_"));
        debug.put("hasRolePrefix", hasRolePrefix);
        
        return debug;
    }
}
