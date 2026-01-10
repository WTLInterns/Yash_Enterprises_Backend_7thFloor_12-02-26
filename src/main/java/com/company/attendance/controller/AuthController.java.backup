package com.company.attendance.controller;

import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.entity.Employee;
import com.company.attendance.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final EmployeeService employeeService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Find employee by email or mobile
            Optional<Employee> employeeOpt;
            
            if (loginRequest.getEmail() != null && !loginRequest.getEmail().trim().isEmpty()) {
                employeeOpt = employeeService.findByEmail(loginRequest.getEmail());
            } else if (loginRequest.getMobile() != null && !loginRequest.getMobile().trim().isEmpty()) {
                employeeOpt = employeeService.findByPhone(loginRequest.getMobile());
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email or mobile number is required"
                ));
            }

            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid credentials"
                ));
            }

            Employee employee = employeeOpt.get();
            
            // Check if password matches (for development, using simple check)
            if (!isValidPassword(loginRequest.getPassword(), employee)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid credentials"
                ));
            }

            // Create response with user data and token
            EmployeeDto employeeDto = convertToDto(employee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", generateToken(employee));
            response.put("user", employeeDto);
            response.put("message", "Login successful");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Login failed: " + e.getMessage()
            ));
        }
    }

    // Simple password validation for development
    private boolean isValidPassword(String inputPassword, Employee employee) {
        // For development, check against common passwords based on role
        String roleName = employee.getRole() != null ? employee.getRole().getName() : "";
        
        switch (roleName) {
            case "Admin":
                return "admin123".equals(inputPassword);
            case "Manager":
                return "manager123".equals(inputPassword);
            case "Employee":
                return "employee123".equals(inputPassword);
            default:
                return "password123".equals(inputPassword);
        }
    }

    // Simple token generation (in production, use JWT)
    private String generateToken(Employee employee) {
        return "token_" + employee.getId() + "_" + System.currentTimeMillis();
    }

    // Convert Employee to EmployeeDto
    private EmployeeDto convertToDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .employeeId(employee.getEmployeeId())
                .employeeCode(employee.getEmployeeCode())
                .roleId(employee.getRole() != null ? employee.getRole().getId() : null)
                .roleName(employee.getRole() != null ? employee.getRole().getName() : null)
                .teamId(employee.getTeam() != null ? employee.getTeam().getId() : null)
                .teamName(employee.getTeam() != null ? employee.getTeam().getName() : null)
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .status(employee.getStatus().name())
                .profileImageUrl(employee.getProfileImageUrl())
                .hiredAt(employee.getHiredAt())
                .profileImageBase64("") // For frontend image display
                .build();
    }

    // Request DTO for login
    public static class LoginRequest {
        private String email;
        private String mobile;
        private String password;
        private Long organizationId;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    }
}
