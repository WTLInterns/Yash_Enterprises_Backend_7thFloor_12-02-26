package com.company.attendance.crm.service;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.crm.repository.ActivityRepository;
import com.company.attendance.crm.entity.Activity;
import com.company.attendance.crm.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final EmployeeRepository employeeRepository;
    private final ActivityRepository activityRepository;
    
    public Integer getCurrentUserId() {
        try {
            // First try to get from X-User-Id header
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                if (request != null) {
                    String userIdHeader = request.getHeader("X-User-Id");
                    if (userIdHeader != null && !userIdHeader.isEmpty()) {
                        return Integer.parseInt(userIdHeader);
                    }
                }
            }
            
            // Fallback to Spring Security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // Try to get user ID from authentication principal
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof String) {
                    // If principal is username, find employee by email
                    Employee employee = employeeRepository.findByEmail((String) principal)
                        .orElse(null);
                    return employee != null ? employee.getId().intValue() : 1;
                } else if (principal instanceof Employee) {
                    // If principal is Employee entity
                    return ((Employee) principal).getId().intValue();
                }
            }
        } catch (Exception e) {
            // Log error but don't fail
        }
        
        // Fallback to admin user
        return 1;
    }
    
    public String getCurrentUserName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof String) {
                    return (String) principal;
                } else if (principal instanceof Employee) {
                    Employee employee = (Employee) principal;
                    return employee.getFirstName() + " " + employee.getLastName();
                }
            }
        } catch (Exception e) {
            // Log error but don't fail
        }
        
        // Fallback to admin user name
        return "Admin User";
    }
    
    public String getUserName(Long userId) {
        if (userId == null) return null;
        try {
            return employeeRepository.findById(userId)
                .map(e -> {
                    String first = e.getFirstName();
                    String last  = e.getLastName();

                    if (first != null && !first.isBlank()) {
                        String full = (first + " " + (last == null ? "" : last)).trim();
                        return full;
                    }

                    String email = e.getEmail();
                    if (email != null && !email.isBlank()) {
                        return email;
                    }

                    return "User " + userId;
                })
                .orElse("User " + userId);
        } catch (Exception e) {
            return "User " + userId;
        }
    }
    
    public void setAuditFields(Object entity) {
        try {
            // Use reflection to set audit fields if they exist
            entity.getClass().getMethod("setCreatedBy", Integer.class).invoke(entity, getCurrentUserId());
            entity.getClass().getMethod("setCreatedAt", Instant.class).invoke(entity, Instant.now());
        } catch (Exception e) {
            // Ignore if fields don't exist
        }
    }
    
    public void updateAuditFields(Object entity) {
        try {
            entity.getClass().getMethod("setUpdatedBy", Integer.class).invoke(entity, getCurrentUserId());
            entity.getClass().getMethod("setUpdatedAt", Instant.class).invoke(entity, Instant.now());
        } catch (Exception e) {
            // Ignore if fields don't exist
        }
    }
    
    public Employee getCurrentUser(Integer userId) {
        return employeeRepository.findById(userId.longValue()).orElse(null);
    }
    
    public void logActivity(Long clientId, String action, String description, Long userId) {
        try {
            // Since Activity entity requires a Deal, and expenses are client-specific,
            // we'll just log to console for now
            // For a proper implementation, you might need a separate ExpenseLog entity
            // or modify Activity to make deal optional and add EXPENSE type
            System.out.println("Activity logged: " + action + " - " + description + " (Client: " + clientId + ", User: " + userId + ")");
        } catch (Exception e) {
            // Don't fail the main request if audit logging fails
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
}
