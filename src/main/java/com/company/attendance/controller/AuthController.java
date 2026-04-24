package com.company.attendance.controller;

import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.dto.LoginRequest;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        try {
            log.info("Login attempt for email: {}", loginRequest.getEmail());

            Employee employee = employeeRepository.findByEmailWithDepartment(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!isValidPassword(loginRequest.getPassword(), employee)) {
                throw new BadCredentialsException("Invalid credentials");
            }

            String roleName = employee.getRole() != null
                    ? employee.getRole().getName().toUpperCase().trim()
                    : "EMPLOYEE";

            EmployeeDto employeeDto = convertToDto(employee);

            Map<String, Object> response = new HashMap<>();
            response.put("user", employeeDto);
            response.put("role", roleName);
            response.put("employeeId", employee.getId());
            response.put("message", "Login successful");

            log.info("Login successful for email: {}, role: {}", loginRequest.getEmail(), roleName);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Login failed for email: {}", loginRequest.getEmail());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            log.error("Login failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            if (registerRequest.getPassword() == null || registerRequest.getPassword().length() < 6)
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
            if (employeeService.findByEmail(registerRequest.getEmail()).isPresent())
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));

            Optional<Role> roleOpt = roleService.findByName(registerRequest.getRoleName());
            if (roleOpt.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + registerRequest.getRoleName()));

            Organization organization = null;
            if (registerRequest.getOrganization() != null && !registerRequest.getOrganization().trim().isEmpty())
                organization = organizationService.findByNameOrCreate(registerRequest.getOrganization());

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
                    .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                    .attendanceAllowed(true)
                    .hiredAt(java.time.LocalDate.now())
                    .createdBy("SYSTEM")
                    .build();

            Employee savedEmployee = employeeService.save(employee);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("user", convertToDto(savedEmployee));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest r) {
        r.setRoleName("ADMIN"); return register(r);
    }

    @PostMapping("/register/manager")
    public ResponseEntity<?> registerManager(@RequestBody RegisterRequest r) {
        r.setRoleName("MANAGER"); return register(r);
    }

    @PostMapping("/register/employee")
    public ResponseEntity<?> registerEmployee(@RequestBody RegisterRequest r) {
        r.setRoleName("EMPLOYEE"); return register(r);
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles() {
        try { return ResponseEntity.ok(roleService.findAllActiveRoles()); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/organizations")
    public ResponseEntity<?> getOrganizations() {
        try { return ResponseEntity.ok(organizationService.findAll()); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage())); }
    }

    private boolean isValidPassword(String inputPassword, Employee employee) {
        String stored = employee.getPasswordHash();
        if (stored == null || stored.isBlank()) return legacyLogin(employee, inputPassword);
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) return passwordEncoder.matches(inputPassword, stored);
        if (stored.equals(inputPassword)) { upgradePassword(employee, inputPassword); return true; }
        if (employee.getPhone() != null && employee.getPhone().equals(inputPassword)) { upgradePassword(employee, inputPassword); return true; }
        return false;
    }

    private boolean legacyLogin(Employee employee, String inputPassword) {
        if (employee.getPhone() != null && employee.getPhone().equals(inputPassword)) { upgradePassword(employee, inputPassword); return true; }
        if ("DEFAULT@123".equals(inputPassword)) { upgradePassword(employee, inputPassword); return true; }
        return false;
    }

    private void upgradePassword(Employee employee, String rawPassword) {
        employee.setPasswordHash(passwordEncoder.encode(rawPassword));
        employeeService.save(employee);
    }

    private String generateUserId() { return "USER_" + System.currentTimeMillis(); }
    private String generateEmployeeId() { return "EMP_" + System.currentTimeMillis(); }

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
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : employee.getDepartmentName())
                .status(employee.getStatus().name())
                .profileImageUrl(employee.getProfileImageUrl())
                .hiredAt(employee.getHiredAt())
                .profileImageBase64("")
                .build();
    }

    public static class RegisterRequest {
        private String name, email, password, phone, organization, roleName;
        public String getName() { return name; } public void setName(String n) { this.name = n; }
        public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
        public String getPassword() { return password; } public void setPassword(String p) { this.password = p; }
        public String getPhone() { return phone; } public void setPhone(String p) { this.phone = p; }
        public String getOrganization() { return organization; } public void setOrganization(String o) { this.organization = o; }
        public String getRoleName() { return roleName; } public void setRoleName(String r) { this.roleName = r; }
    }
}
