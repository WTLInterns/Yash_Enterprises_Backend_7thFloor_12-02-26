package com.company.attendance.service;

import com.company.attendance.entity.Employee;
import com.company.attendance.entity.Role;
import com.company.attendance.entity.Team;
import com.company.attendance.entity.Designation;
import com.company.attendance.entity.Organization;
import com.company.attendance.entity.Department;
import com.company.attendance.entity.Shift;
import com.company.attendance.repository.AttendanceRepository;
import com.company.attendance.repository.ExpenseRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.RoleRepository;
import com.company.attendance.repository.TeamRepository;
import com.company.attendance.repository.DesignationRepository;
import com.company.attendance.repository.OrganizationRepository;
import com.company.attendance.repository.DepartmentRepository;
import com.company.attendance.repository.ShiftRepository;
import com.company.attendance.dto.EmployeeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final DesignationRepository designationRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final ShiftRepository shiftRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExpenseRepository expenseRepository;

    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            employee.setCreatedAt(LocalDateTime.now());
        }
        employee.setUpdatedAt(LocalDateTime.now());
        return employeeRepository.save(employee);
    }

    public Employee createFromDto(EmployeeDto dto) {
        System.out.println("=== EMPLOYEE CREATE DEBUG ===");
        System.out.println("Received DTO: " + dto);
        
        Employee employee = new Employee();
        
        // Set basic fields
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setEmployeeId(dto.getEmployeeId());
        employee.setUserId(dto.getUserId());
        employee.setEmployeeCode(dto.getEmployeeCode());
        
        // Set relationships from DTO IDs
        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId()).orElse(null);
            System.out.println("Setting role: " + role);
            employee.setRole(role);
        }
        
        if (dto.getTeamId() != null) {
            Team team = teamRepository.findById(dto.getTeamId()).orElse(null);
            System.out.println("Setting team: " + team);
            employee.setTeam(team);
        }
        
        // Set designation (handle custom designation)
        if (dto.getDesignationId() != null) {
            Designation designation = designationRepository.findById(dto.getDesignationId()).orElse(null);
            System.out.println("Setting designation: " + designation);
            employee.setDesignation(designation);
        }
        
        // Set custom designation if provided
        if (dto.getCustomDesignation() != null && !dto.getCustomDesignation().trim().isEmpty()) {
            System.out.println("Setting custom designation: " + dto.getCustomDesignation());
            employee.setCustomDesignation(dto.getCustomDesignation());
        }
        
        if (dto.getReportingManagerId() != null) {
            Employee reportingManager = employeeRepository.findById(dto.getReportingManagerId()).orElse(null);
            System.out.println("Setting reportingManager: " + reportingManager);
            employee.setReportingManager(reportingManager);
        }
        
        // ✅ NEW: Handle TL assignment for EMPLOYEE role
        if (dto.getTlId() != null) {

            Employee tl = employeeRepository.findById(dto.getTlId())
                .orElseThrow(() -> new RuntimeException("TL not found"));

            employee.setTl(tl);

            // 🔥 inherit TL department
            Department resolvedDepartment = tl.getDepartment();
            if (resolvedDepartment == null && tl.getDepartmentName() != null && !tl.getDepartmentName().trim().isEmpty()) {
                resolvedDepartment = departmentRepository.findByCode(tl.getDepartmentName().trim()).orElse(null);
            }

            employee.setDepartment(resolvedDepartment);
            employee.setDepartmentName(tl.getDepartmentName());

            System.out.println("Employee inherited department from TL: " + tl.getDepartmentName());
        }
        
        if (dto.getOrganizationId() != null) {
            Organization organization = organizationRepository.findById(dto.getOrganizationId()).orElse(null);
            System.out.println("Setting organization: " + organization);
            employee.setOrganization(organization);
        }
        
        // ✅ CRITICAL FIX: Handle TL department logic
        if (employee.getRole() != null && "TL".equalsIgnoreCase(employee.getRole().getName())) {
            // TL → department comes from CRM (string like "PPO", "PPE", "HLC")
            employee.setDepartment(null);
            employee.setDepartmentName(dto.getDepartmentName());
            System.out.println("Setting TL departmentName: " + dto.getDepartmentName());
        } else {
            // Non-TL → normal HR department (FK relationship)
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
                System.out.println("Setting department: " + department);
                employee.setDepartment(department);
            }
            employee.setDepartmentName(null);
            
            // ✅ ENSURE TL IS SET FOR EMPLOYEE ROLE
            if (employee.getRole() != null && "EMPLOYEE".equalsIgnoreCase(employee.getRole().getName()) && dto.getTlId() != null) {
                Employee tl = employeeRepository.findById(dto.getTlId()).orElse(null);
                System.out.println("Setting TL for EMPLOYEE: " + tl);
                employee.setTl(tl);
            }
        }
        
        if (dto.getShiftId() != null) {
            Shift shift = shiftRepository.findById(dto.getShiftId()).orElse(null);
            System.out.println("Setting shift: " + shift);
            employee.setShift(shift);
        }
        
        // Set status and other fields
        if (dto.getStatus() != null) {
            employee.setStatus(Employee.Status.valueOf(dto.getStatus()));
        }
        employee.setAttendanceAllowed(dto.getAttendanceAllowed());
        employee.setHiredAt(dto.getHiredAt());
        employee.setTerminationDate(dto.getTerminationDate());
        
        // Set additional fields
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setGender(dto.getGender());
        employee.setPanNumber(dto.getPanNumber());
        employee.setBankAccountNumber(dto.getBankAccountNumber());
        
        // Handle profile image if provided as base64
        if (dto.getProfileImageBase64() != null && !dto.getProfileImageBase64().trim().isEmpty()) {
            try {
                // Decode base64 and save image
                String base64Data = dto.getProfileImageBase64();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1]; // Remove data URL prefix
                }
                
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                String fileName = System.currentTimeMillis() + "_profile.jpg";
                String imageUrl = "/uploads/employees/" + fileName;
                
                // Create uploads directory if it doesn't exist
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/employees");
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                
                // Save image file
                java.nio.file.Files.write(uploadPath.resolve(fileName), imageBytes);
                
                // Set image URL
                employee.setProfileImageUrl(imageUrl);
                System.out.println("Profile image saved: " + imageUrl);
            } catch (Exception e) {
                System.out.println("Error saving profile image: " + e.getMessage());
                // Continue without image if there's an error
            }
        }
        
        // Set timestamps
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Final employee before save: " + employee);
        Employee saved = employeeRepository.save(employee);
        System.out.println("Saved employee: " + saved);
        System.out.println("=== END EMPLOYEE CREATE DEBUG ===");
        
        return saved;
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findByIdWithRelationships(id);
    }

    public Optional<Employee> findByUserId(String userId) {
        return employeeRepository.findByUserId(userId);
    }

    public Optional<Employee> findByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public Optional<Employee> findByPhone(String phone) {
        return employeeRepository.findByPhone(phone);
    }

    public List<Employee> findAll() {
        return employeeRepository.findAllWithRelationships();
    }

    public List<Employee> findByRole(String roleName) {
        return employeeRepository.findByRole_Name(roleName);
    }

    public List<Employee> findByIsActive(Boolean isActive) {
        if (isActive == null) {
            return employeeRepository.findAll();
        }
        Employee.Status status = isActive ? Employee.Status.ACTIVE : Employee.Status.INACTIVE;
        return employeeRepository.findByStatus(status);
    }

    @Transactional
    public Employee update(Long id, EmployeeDto dto) {
        System.out.println("=== EMPLOYEE UPDATE DEBUG ===");
        System.out.println("Updating employee ID: " + id);
        System.out.println("Received DTO: " + dto);
        
        Employee existing = findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        System.out.println("Existing data: " + existing);
        
        // Update basic fields
        if (dto.getFirstName() != null && !dto.getFirstName().equals(existing.getFirstName())) {
            System.out.println("Updating firstName from '" + existing.getFirstName() + "' to '" + dto.getFirstName() + "'");
            existing.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().equals(existing.getLastName())) {
            System.out.println("Updating lastName from '" + existing.getLastName() + "' to '" + dto.getLastName() + "'");
            existing.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            System.out.println("Updating email from '" + existing.getEmail() + "' to '" + dto.getEmail() + "'");
            existing.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().equals(existing.getPhone())) {
            System.out.println("Updating phone from '" + existing.getPhone() + "' to '" + dto.getPhone() + "'");
            existing.setPhone(dto.getPhone());
        }
        if (dto.getEmployeeId() != null && !dto.getEmployeeId().equals(existing.getEmployeeId())) {
            existing.setEmployeeId(dto.getEmployeeId());
        }
        if (dto.getUserId() != null && !dto.getUserId().equals(existing.getUserId())) {
            existing.setUserId(dto.getUserId());
        }
        if (dto.getEmployeeCode() != null && !dto.getEmployeeCode().equals(existing.getEmployeeCode())) {
            existing.setEmployeeCode(dto.getEmployeeCode());
        }
        
        // Update relationships from DTO IDs
        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId()).orElse(null);
            if (role != null && !role.equals(existing.getRole())) {
                System.out.println("Updating role from '" + existing.getRole() + "' to '" + role + "'");
                existing.setRole(role);
            }
        }
        
        if (dto.getTeamId() != null) {
            Team team = teamRepository.findById(dto.getTeamId()).orElse(null);
            if (team != null && !team.equals(existing.getTeam())) {
                System.out.println("Updating team from '" + existing.getTeam() + "' to '" + team + "'");
                existing.setTeam(team);
            }
        }
        
        if (dto.getDesignationId() != null) {
            Designation designation = designationRepository.findById(dto.getDesignationId()).orElse(null);
            if (designation != null && !designation.equals(existing.getDesignation())) {
                System.out.println("Updating designation from '" + existing.getDesignation() + "' to '" + designation + "'");
                existing.setDesignation(designation);
            }
        }
        
        if (dto.getReportingManagerId() != null) {
            Employee reportingManager = employeeRepository.findById(dto.getReportingManagerId()).orElse(null);
            if (reportingManager != null && !reportingManager.equals(existing.getReportingManager())) {
                System.out.println("Updating reportingManager from '" + existing.getReportingManager() + "' to '" + reportingManager + "'");
                existing.setReportingManager(reportingManager);
            }
        }
        
        // ✅ NEW: Handle TL assignment for EMPLOYEE role during update
        if (dto.getTlId() != null) {

            Employee tl = employeeRepository.findById(dto.getTlId())
                .orElseThrow(() -> new RuntimeException("TL not found"));

            System.out.println("Updating TL from '" + existing.getTl() + "' to '" + tl + "'");
            existing.setTl(tl);

            // 🔥 update department as well
            Department resolvedDepartment = tl.getDepartment();
            if (resolvedDepartment == null && tl.getDepartmentName() != null && !tl.getDepartmentName().trim().isEmpty()) {
                resolvedDepartment = departmentRepository.findByCode(tl.getDepartmentName().trim()).orElse(null);
            }

            existing.setDepartment(resolvedDepartment);
            existing.setDepartmentName(tl.getDepartmentName());
        }
        
        if (dto.getOrganizationId() != null) {
            Organization organization = organizationRepository.findById(dto.getOrganizationId()).orElse(null);
            if (organization != null && !organization.equals(existing.getOrganization())) {
                System.out.println("Updating organization from '" + existing.getOrganization() + "' to '" + organization + "'");
                existing.setOrganization(organization);
            }
        }
        
        // ✅ CRITICAL FIX: Handle TL department logic for UPDATE
        if (existing.getRole() != null && "TL".equalsIgnoreCase(existing.getRole().getName())) {
            // TL update → use departmentName string
            String newDepartmentName = dto.getDepartmentName();
            if (newDepartmentName != null && !newDepartmentName.equals(existing.getDepartmentName())) {
                System.out.println("Updating TL departmentName from '" + existing.getDepartmentName() + "' to '" + newDepartmentName + "'");
                existing.setDepartment(null);
                existing.setDepartmentName(newDepartmentName);
            }
        } else {
            // Non-TL update → use department FK
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
                if (department != null && !department.equals(existing.getDepartment())) {
                    System.out.println("Updating department from '" + existing.getDepartment() + "' to '" + department + "'");
                    existing.setDepartment(department);
                    existing.setDepartmentName(null); // Clear departmentName for non-TL
                }
            }
            
            // ✅ ENSURE TL IS UPDATED FOR EMPLOYEE ROLE
            if (existing.getRole() != null && "EMPLOYEE".equalsIgnoreCase(existing.getRole().getName()) && dto.getTlId() != null) {
                Employee tl = employeeRepository.findById(dto.getTlId()).orElse(null);
                if (tl != null && !tl.equals(existing.getTl())) {
                    System.out.println("Updating TL for EMPLOYEE from '" + existing.getTl() + "' to '" + tl + "'");
                    existing.setTl(tl);
                }
            }
        }
        
        if (dto.getShiftId() != null) {
            Shift shift = shiftRepository.findById(dto.getShiftId()).orElse(null);
            if (shift != null && !shift.equals(existing.getShift())) {
                System.out.println("Updating shift from '" + existing.getShift() + "' to '" + shift + "'");
                existing.setShift(shift);
            }
        }
        
        if (dto.getStatus() != null && !dto.getStatus().equals(existing.getStatus().name())) {
            System.out.println("Updating status from '" + existing.getStatus() + "' to '" + dto.getStatus() + "'");
            existing.setStatus(Employee.Status.valueOf(dto.getStatus()));
        }
        if (dto.getAttendanceAllowed() != null && !dto.getAttendanceAllowed().equals(existing.getAttendanceAllowed())) {
            System.out.println("Updating attendanceAllowed from '" + existing.getAttendanceAllowed() + "' to '" + dto.getAttendanceAllowed() + "'");
            existing.setAttendanceAllowed(dto.getAttendanceAllowed());
        }
        if (dto.getHiredAt() != null && !dto.getHiredAt().equals(existing.getHiredAt())) {
            System.out.println("Updating hiredAt from '" + existing.getHiredAt() + "' to '" + dto.getHiredAt() + "'");
            existing.setHiredAt(dto.getHiredAt());
        }
        if (dto.getTerminationDate() != null && !dto.getTerminationDate().equals(existing.getTerminationDate())) {
            System.out.println("Updating terminationDate from '" + existing.getTerminationDate() + "' to '" + dto.getTerminationDate() + "'");
            existing.setTerminationDate(dto.getTerminationDate());
        }
        
        // Update custom designation
        if (dto.getCustomDesignation() != null && !dto.getCustomDesignation().equals(existing.getCustomDesignation())) {
            System.out.println("Updating custom designation from '" + existing.getCustomDesignation() + "' to '" + dto.getCustomDesignation() + "'");
            existing.setCustomDesignation(dto.getCustomDesignation());
        }
        
        // Update date of birth
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().equals(existing.getDateOfBirth())) {
            existing.setDateOfBirth(dto.getDateOfBirth());
        }
        
        // Update gender
        if (dto.getGender() != null && !dto.getGender().equals(existing.getGender())) {
            existing.setGender(dto.getGender());
        }

        // Update PAN and bank account
        if (dto.getPanNumber() != null) existing.setPanNumber(dto.getPanNumber());
        if (dto.getBankAccountNumber() != null) existing.setBankAccountNumber(dto.getBankAccountNumber());
        
        // Handle profile image update if provided as base64
        if (dto.getProfileImageBase64() != null && !dto.getProfileImageBase64().trim().isEmpty()) {
            try {
                // Decode base64 and save image
                String base64Data = dto.getProfileImageBase64();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1]; // Remove data URL prefix
                }
                
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                String fileName = System.currentTimeMillis() + "_profile.jpg";
                String imageUrl = "/uploads/employees/" + fileName;
                
                // Create uploads directory if it doesn't exist
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/employees");
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                
                // Save image file
                java.nio.file.Files.write(uploadPath.resolve(fileName), imageBytes);
                
                // Set image URL
                existing.setProfileImageUrl(imageUrl);
                System.out.println("Profile image updated: " + imageUrl);
            } catch (Exception e) {
                System.out.println("Error updating profile image: " + e.getMessage());
                // Continue without image if there's an error
            }
        }
        
        // IMPORTANT: Only update updated_at, never touch created_at and created_by
        existing.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Final existing data before save: " + existing);
        Employee saved = employeeRepository.save(existing);
        System.out.println("Saved data: " + saved);
        System.out.println("=== END EMPLOYEE UPDATE DEBUG ===");
        
        return saved;
    }

    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    /** Soft delete — marks INACTIVE, no FK violation */
    @Transactional
    public void deactivate(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
        emp.setStatus(Employee.Status.INACTIVE);
        emp.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(emp);
    }

    /** Hard delete — removes all FK dependencies then deletes employee */
    @Transactional
    public void deleteWithCleanup(Long id) {
        // 1. Null out self-referencing FKs: other employees who have this as TL or reporting manager
        List<Employee> subordinates = employeeRepository.findAll().stream()
                .filter(e -> (e.getTl() != null && e.getTl().getId().equals(id))
                          || (e.getReportingManager() != null && e.getReportingManager().getId().equals(id)))
                .collect(java.util.stream.Collectors.toList());
        for (Employee sub : subordinates) {
            if (sub.getTl() != null && sub.getTl().getId().equals(id)) sub.setTl(null);
            if (sub.getReportingManager() != null && sub.getReportingManager().getId().equals(id)) sub.setReportingManager(null);
            employeeRepository.save(sub);
        }
        // 2. Delete attendance records
        attendanceRepository.deleteByEmployeeId(id);
        // 3. Delete expense records
        expenseRepository.deleteByEmployeeId(id);
        // 4. Delete the employee
        employeeRepository.deleteById(id);
    }

    public boolean existsByUserId(String userId) {
        return employeeRepository.existsByUserId(userId);
    }

    public boolean existsByEmployeeId(String employeeId) {
        return employeeRepository.existsByEmployeeId(employeeId);
    }
    
    public String generateNextEmployeeId() {
        List<Employee> employees = employeeRepository.findAll();
        int maxId = 0;
        for (Employee emp : employees) {
            if (emp.getEmployeeId() != null) {
                try {
                    String upper = emp.getEmployeeId().toUpperCase();
                    String numericPart = upper.startsWith("YE")
                        ? upper.substring(2)
                        : emp.getEmployeeId().replaceAll("[^0-9]", "");
                    if (!numericPart.isEmpty()) {
                        int id = Integer.parseInt(numericPart);
                        if (id > maxId) maxId = id;
                    }
                } catch (NumberFormatException e) { /* skip */ }
            }
        }
        return "YE" + (maxId + 1);
    }
}
