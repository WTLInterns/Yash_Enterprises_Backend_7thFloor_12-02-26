package com.company.attendance.service;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 🔥 CENTRALIZED DEPARTMENT-AWARE SERVICE
 * 
 * Handles role and department derivation from headers or DB fallback.
 * Prevents code duplication across controllers.
 */
@Service
@RequiredArgsConstructor
public class DepartmentAwareService {

    private final EmployeeRepository employeeRepository;

    /**
     * User context with derived role and department
     */
    public static class UserContext {
        public final String userId;
        public final String role;
        public final String department;
        public final boolean isAdmin;

        public UserContext(String userId, String role, String department) {
            this.userId = userId;
            this.role = role;
            this.department = department;
            this.isAdmin = "ADMIN".equals(role);
        }
    }

    /**
     * 🔥 Get user context with header fallback to DB
     */
    public UserContext getUserContext(
            String userId, 
            String userRole, 
            String userDepartment) {
        
        // If headers are complete, use them
        if (userId != null && userRole != null && userDepartment != null) {
            return new UserContext(userId, userRole, userDepartment);
        }
        
        // 🔥 FALLBACK: Derive from employeeId if headers missing
        if (userId != null) {
            try {
                Employee employee = employeeRepository.findById(Long.valueOf(userId))
                    .orElse(null);
                
                if (employee != null) {
                    return new UserContext(
                        userId, 
                        employee.getRole() != null ? employee.getRole().getName() : null, 
                        employee.getDepartment() != null ? employee.getDepartment().getName() : null
                    );
                }
            } catch (NumberFormatException e) {
                // Invalid userId format
            }
        }
        
        // 🔥 FALLBACK: Try to get from current request
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
            
            String headerUserId = request.getHeader("X-User-Id");
            if (headerUserId != null && !headerUserId.equals(userId)) {
                // Different user in headers, use that
                return getUserContext(headerUserId, 
                    request.getHeader("X-User-Role"),
                    request.getHeader("X-User-Department"));
            }
        } catch (Exception e) {
            // No request context available
        }
        
        // No valid context found
        return null;
    }

    /**
     * 🔥 Check if user can access department
     */
    public boolean canAccessDepartment(UserContext context, String targetDepartment) {
        if (context == null || targetDepartment == null) {
            return false;
        }
        
        // Admin can access any department
        if (context.isAdmin) {
            return true;
        }
        
        // Others can only access their own department
        return targetDepartment.equals(context.department);
    }

    /**
     * 🔥 Get effective department for queries
     */
    public String getEffectiveDepartment(UserContext context, String requestedDepartment) {
        if (context == null) {
            return null;
        }
        
        // Admin can use requested department
        if (context.isAdmin) {
            return requestedDepartment;
        }
        
        // Others are forced to their own department
        return context.department;
    }
}
