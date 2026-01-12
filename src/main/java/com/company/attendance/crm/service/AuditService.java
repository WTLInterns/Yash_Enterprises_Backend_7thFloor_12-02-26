package com.company.attendance.crm.service;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final EmployeeRepository employeeRepository;
    
    public Integer getCurrentUserId() {
        try {
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
}
