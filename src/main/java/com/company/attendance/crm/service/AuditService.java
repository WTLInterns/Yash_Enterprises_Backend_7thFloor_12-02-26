package com.company.attendance.crm.service;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.crm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final EmployeeRepository employeeRepository;
    private final ActivityRepository activityRepository;

    public Integer getCurrentUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isBlank()) {
                    return Integer.parseInt(userIdHeader.trim());
                }
            }
        } catch (Exception ignored) {}
        return 1; // fallback to admin
    }

    public String getCurrentUserName() {
        try {
            Integer userId = getCurrentUserId();
            return employeeRepository.findById(userId.longValue())
                .map(e -> {
                    String first = e.getFirstName() != null ? e.getFirstName() : "";
                    String last  = e.getLastName()  != null ? e.getLastName()  : "";
                    return (first + " " + last).trim();
                })
                .orElse("Admin User");
        } catch (Exception ignored) {}
        return "Admin User";
    }

    public String getUserName(Long userId) {
        if (userId == null) return null;
        try {
            return employeeRepository.findById(userId)
                .map(e -> {
                    String first = e.getFirstName();
                    String last  = e.getLastName();
                    if (first != null && !first.isBlank())
                        return (first + " " + (last == null ? "" : last)).trim();
                    String email = e.getEmail();
                    return (email != null && !email.isBlank()) ? email : "User " + userId;
                })
                .orElse("User " + userId);
        } catch (Exception ignored) {
            return "User " + userId;
        }
    }

    public void setAuditFields(Object entity) {
        try {
            entity.getClass().getMethod("setCreatedBy", Integer.class).invoke(entity, getCurrentUserId());
            entity.getClass().getMethod("setCreatedAt", Instant.class).invoke(entity, Instant.now());
        } catch (Exception ignored) {}
    }

    public void updateAuditFields(Object entity) {
        try {
            entity.getClass().getMethod("setUpdatedBy", Integer.class).invoke(entity, getCurrentUserId());
            entity.getClass().getMethod("setUpdatedAt", Instant.class).invoke(entity, Instant.now());
        } catch (Exception ignored) {}
    }

    public Employee getCurrentUser(Integer userId) {
        return employeeRepository.findById(userId.longValue()).orElse(null);
    }

    public void logActivity(Long clientId, String action, String description, Long userId) {
        System.out.println("Activity: " + action + " - " + description + " (Client: " + clientId + ", User: " + userId + ")");
    }
}
