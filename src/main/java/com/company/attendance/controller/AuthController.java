package com.company.attendance.controller;

import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.Role;
import com.company.attendance.entity.Organization;
import com.company.attendance.service.EmployeeService;
import com.company.attendance.service.RoleService;
import com.company.attendance.service.OrganizationService;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final EmployeeService employeeService;
    private final RoleService roleService;
    private final OrganizationService organizationService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        try {
            // Find employee by email
            String email = loginRequest.getEmail();
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email is required"
                ));
            }

            Optional<Employee> employeeOpt = employeeService.findByEmail(email);

            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid credentials"
                ));
            }

            Employee employee = employeeOpt.get();
            
            // Log current FCM tokens
            log.info("Employee {} current FCM tokens - Mobile: {}, Web: {}", 
                    employee.getId(),
                    employee.getFcmTokenMobile() != null ? employee.getFcmTokenMobile().substring(0, Math.min(20, employee.getFcmTokenMobile().length())) + "..." : "NULL",
                    employee.getFcmTokenWeb() != null ? employee.getFcmTokenWeb().substring(0, Math.min(20, employee.getFcmTokenWeb().length())) + "..." : "NULL");
            
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
            
            log.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Login failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Validate required fields
            if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Name is required"
                ));
            }
            
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email is required"
                ));
            }
            
            if (registerRequest.getPassword() == null || registerRequest.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Password must be at least 6 characters"
                ));
            }

            // Check if email already exists
            if (employeeService.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email already exists"
                ));
            }

            // Find role by name
            Optional<Role> roleOpt = roleService.findByName(registerRequest.getRoleName());
            if (roleOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid role: " + registerRequest.getRoleName()
                ));
            }

            // Find or create organization
            Organization organization = null;
            if (registerRequest.getOrganization() != null && !registerRequest.getOrganization().trim().isEmpty()) {
                organization = organizationService.findByNameOrCreate(registerRequest.getOrganization());
            }

            // Create new employee
            Employee employee = Employee.builder()
                    .userId(generateUserId())
                    .employeeId(generateEmployeeId())
                    .firstName(registerRequest.getName())
                    .lastName("")
                    .email(registerRequest.getEmail())
                    .phone(registerRequest.getPhone() != null ? registerRequest.getPhone() : "")
                    .role(roleOpt.get())
                    .organization(organization)
                    .status(Employee.Status.ACTIVE)
                    .passwordHash(hashPassword(registerRequest.getPassword()))
                    .attendanceAllowed(true)
                    .hiredAt(java.time.LocalDate.now())
                    .createdBy("SYSTEM")
                    .build();

            Employee savedEmployee = employeeService.save(employee);
            
            // Return success response
            EmployeeDto employeeDto = convertToDto(savedEmployee);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("user", employeeDto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Registration failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            // Force role to ADMIN for admin registration
            registerRequest.setRoleName("ADMIN");
            return register(registerRequest);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Admin registration failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/register/manager")
    public ResponseEntity<?> registerManager(@RequestBody RegisterRequest registerRequest) {
        try {
            // Force role to MANAGER for manager registration
            registerRequest.setRoleName("MANAGER");
            return register(registerRequest);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Manager registration failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/register/employee")
    public ResponseEntity<?> registerEmployee(@RequestBody RegisterRequest registerRequest) {
        try {
            // Force role to EMPLOYEE for employee registration
            registerRequest.setRoleName("EMPLOYEE");
            return register(registerRequest);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Employee registration failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles() {
        try {
            List<Role> roles = roleService.findAllActiveRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to fetch roles: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/organizations")
    public ResponseEntity<?> getOrganizations() {
        try {
            List<Organization> organizations = organizationService.findAll();
            return ResponseEntity.ok(organizations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to fetch organizations: " + e.getMessage()
            ));
        }
    }

    // BCrypt password validation
    private boolean isValidPassword(String inputPassword, Employee employee) {
        
        String stored = employee.getPasswordHash();
        
        // 1. No password at all -> allow phone / default
        if (stored == null || stored.isBlank()) {
            return legacyLoginAndUpgrade(employee, inputPassword);
        }
        
        // 2. BCrypt password
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
            return passwordEncoder.matches(inputPassword, stored);
        }
        
        // 3. Legacy plain-text password (VERY IMPORTANT FIX)
        if (stored.equals(inputPassword)) {
            upgradePassword(employee, inputPassword);
            return true;
        }
        
        // 4. Phone number login fallback
        if (employee.getPhone() != null && employee.getPhone().equals(inputPassword)) {
            upgradePassword(employee, inputPassword);
            return true;
        }
        
        return false;
    }
    
    private boolean legacyLoginAndUpgrade(Employee employee, String inputPassword) {
        
        if (employee.getPhone() != null && employee.getPhone().equals(inputPassword)) {
            upgradePassword(employee, inputPassword);
            return true;
        }
        
        if ("DEFAULT@123".equals(inputPassword)) {
            upgradePassword(employee, inputPassword);
            return true;
        }
        
        return false;
    }
    
    // Auto-upgrade password to BCrypt (IMPORTANT)
    private void upgradePassword(Employee employee, String rawPassword) {
        employee.setPasswordHash(passwordEncoder.encode(rawPassword));
        employeeService.save(employee);
    }

    // Simple token generation (in production, use JWT)
    private String generateToken(Employee employee) {
        return "token_" + employee.getId() + "_" + System.currentTimeMillis();
    }

    // BCrypt password hashing
    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    // Generate unique user ID
    private String generateUserId() {
        return "USER_" + System.currentTimeMillis();
    }

    // Generate unique employee ID
    private String generateEmployeeId() {
        return "EMP_" + System.currentTimeMillis();
    }

    // Convert Employee to EmployeeDto
    private EmployeeDto convertToDto(Employee employee) {
        String fullName = (employee.getFirstName() != null ? employee.getFirstName() : "") + 
                       (employee.getLastName() != null && !employee.getLastName().isEmpty() ? " " + employee.getLastName() : "");
        
        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(fullName.trim())
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
        private String password;
        private String organization;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getOrganization() { return organization; }
        public void setOrganization(String organization) { this.organization = organization; }
    }

    // Request DTO for registration
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String phone;
        private String organization;
        private String roleName;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getOrganization() { return organization; }
        public void setOrganization(String organization) { this.organization = organization; }
        
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }
}
