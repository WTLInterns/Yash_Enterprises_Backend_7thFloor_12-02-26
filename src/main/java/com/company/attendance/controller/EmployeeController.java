package com.company.attendance.controller;

import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.entity.Employee;
import com.company.attendance.mapper.EmployeeMapper;
import com.company.attendance.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;
    @Autowired
    private final EmployeeMapper employeeMapper;

    @GetMapping
public ResponseEntity<List<EmployeeDto>> listEmployees() {
        List<Employee> employees = employeeService.findAll();
        List<EmployeeDto> dtos = employees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/managers")
    public ResponseEntity<List<EmployeeDto>> getManagers() {
        List<Employee> managers = employeeService.findByRole("MANAGER");
        List<EmployeeDto> dtos = managers.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(employeeMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        try {
            System.out.println("Received employee creation request: " + dto);
            
            // Set default values for required fields if not provided
            if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "First name is required"));
            }
            
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            // Set default status if not provided
            if (dto.getStatus() == null) {
                dto.setStatus("ACTIVE");
            }
            
            // Set default organization if not provided
            if (dto.getOrganizationId() == null) {
                dto.setOrganizationId(1L);
            }
            
            // Generate userId if not provided
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                dto.setUserId("emp_" + System.currentTimeMillis());
            }
            
            // Generate employeeId if not provided
            if (dto.getEmployeeId() == null || dto.getEmployeeId().trim().isEmpty()) {
                dto.setEmployeeId("EMP_" + System.currentTimeMillis());
            }
            
            // 🔥 DEPARTMENT VALIDATION: TL must have department
            if ("TL".equals(dto.getRoleName()) && dto.getDepartmentId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Department is required for TL role"));
            }
            
            // 🔥 TL VALIDATION: EMPLOYEE must have TL assigned
            if ("EMPLOYEE".equals(dto.getRoleName()) && dto.getTlId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Team Lead (TL) is required for EMPLOYEE role"));
            }
            
            // 🔥 DEPARTMENT ENFORCEMENT: Non-TL roles get derived department (prevent client tampering)
            if (!"TL".equals(dto.getRoleName()) && dto.getDepartmentId() != null) {
                // For non-TL roles, ignore client-side department and let service derive it
                dto.setDepartmentId(null);
            }
            
            Employee employee = employeeService.createFromDto(dto);
            Employee saved = employeeService.save(employee);
            return ResponseEntity.ok(employeeMapper.toDto(saved));
            
        } catch (Exception e) {
            System.err.println("Error creating employee: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create employee: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        System.out.println("=== CONTROLLER UPDATE DEBUG ===");
        System.out.println("Updating employee ID: " + id);
        System.out.println("Received DTO: " + dto);
        
        Employee updated = employeeService.update(id, dto);
        System.out.println("Updated employee: " + updated);
        
        EmployeeDto responseDto = employeeMapper.toDto(updated);
        System.out.println("Response DTO: " + responseDto);
        System.out.println("=== END CONTROLLER UPDATE DEBUG ===");
        
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportEmployeesToExcel() {
        try {
            List<Employee> employees = employeeService.findAll();
            List<EmployeeDto> employeeDtos = employees.stream()
                    .map(employeeMapper::toDto)
                    .collect(Collectors.toList());
            
            // Create Excel workbook
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Employees");
            
            // Create header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {
                "First Name", "Last Name", "Email", "Phone", "Employee ID", "User ID",
                "Role", "Team", "Designation", "Custom Designation", "Status",
                "Date of Birth", "Gender", "Hire Date", "Attendance Allowed"
            };
            
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // Create data rows
            for (int i = 0; i < employeeDtos.size(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                EmployeeDto emp = employeeDtos.get(i);
                
                row.createCell(0).setCellValue(emp.getFirstName() != null ? emp.getFirstName() : "");
                row.createCell(1).setCellValue(emp.getLastName() != null ? emp.getLastName() : "");
                row.createCell(2).setCellValue(emp.getEmail() != null ? emp.getEmail() : "");
                row.createCell(3).setCellValue(emp.getPhone() != null ? emp.getPhone() : "");
                row.createCell(4).setCellValue(emp.getEmployeeId() != null ? emp.getEmployeeId() : "");
                row.createCell(5).setCellValue(emp.getUserId() != null ? emp.getUserId() : "");
                row.createCell(6).setCellValue(emp.getRoleName() != null ? emp.getRoleName() : "");
                row.createCell(7).setCellValue(emp.getTeamName() != null ? emp.getTeamName() : "");
                row.createCell(8).setCellValue(emp.getDesignationName() != null ? emp.getDesignationName() : "");
                row.createCell(9).setCellValue(emp.getCustomDesignation() != null ? emp.getCustomDesignation() : "");
                row.createCell(10).setCellValue(emp.getStatus() != null ? emp.getStatus() : "");
                row.createCell(11).setCellValue(emp.getDateOfBirth() != null ? emp.getDateOfBirth().toString() : "");
                row.createCell(12).setCellValue(emp.getGender() != null ? emp.getGender() : "");
                row.createCell(13).setCellValue(emp.getHiredAt() != null ? emp.getHiredAt().toString() : "");
                row.createCell(14).setCellValue(emp.getAttendanceAllowed() != null ? emp.getAttendanceAllowed().toString() : "true");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] excelBytes = outputStream.toByteArray();
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=\"employees.xlsx\"")
                    .body(excelBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, Object>> importEmployeesFromExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;
            
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream());
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    EmployeeDto dto = new EmployeeDto();
                    dto.setFirstName(getCellValue(row, 0));
                    dto.setLastName(getCellValue(row, 1));
                    dto.setEmail(getCellValue(row, 2));
                    dto.setPhone(getCellValue(row, 3));
                    dto.setEmployeeId(getCellValue(row, 4));
                    dto.setUserId(getCellValue(row, 5));
                    
                    // Handle role lookup
                    String roleName = getCellValue(row, 6);
                    if (roleName != null && !roleName.isEmpty()) {
                        // Find role by name (simplified)
                        dto.setRoleId(2L); // Default to EMPLOYEE role
                    }
                    
                    // Handle team lookup
                    String teamName = getCellValue(row, 7);
                    if (teamName != null && !teamName.isEmpty()) {
                        dto.setTeamId(1L); // Default to first team
                    }
                    
                    // Handle designation
                    String designationName = getCellValue(row, 8);
                    String customDesignation = getCellValue(row, 9);
                    if (customDesignation != null && !customDesignation.isEmpty()) {
                        dto.setCustomDesignation(customDesignation);
                    } else if (designationName != null && !designationName.isEmpty()) {
                        dto.setDesignationId(1L); // Default to first designation
                    }
                    
                    dto.setStatus(getCellValue(row, 10));
                    
                    // Parse dates
                    String dobStr = getCellValue(row, 11);
                    if (dobStr != null && !dobStr.isEmpty()) {
                        try {
                            dto.setDateOfBirth(java.time.LocalDate.parse(dobStr));
                        } catch (Exception e) {
                            // Skip invalid date
                        }
                    }
                    
                    dto.setGender(getCellValue(row, 12));
                    
                    String hireDateStr = getCellValue(row, 13);
                    if (hireDateStr != null && !hireDateStr.isEmpty()) {
                        try {
                            dto.setHiredAt(java.time.LocalDate.parse(hireDateStr));
                        } catch (Exception e) {
                            // Use current date
                            dto.setHiredAt(java.time.LocalDate.now());
                        }
                    }
                    
                    String attendanceAllowed = getCellValue(row, 14);
                    dto.setAttendanceAllowed(attendanceAllowed == null || attendanceAllowed.equalsIgnoreCase("true"));
                    
                    // Generate employee ID if not provided
                    if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
                        dto.setEmployeeId(employeeService.generateNextEmployeeId());
                    }
                    
                    // Check if employee ID already exists
                    if (employeeService.existsByEmployeeId(dto.getEmployeeId())) {
                        results.add(Map.of(
                            "row", i + 1,
                            "status", "error",
                            "message", "Employee ID already exists: " + dto.getEmployeeId()
                        ));
                        errorCount++;
                        continue;
                    }
                    
                    // Save employee
                    Employee employee = employeeService.createFromDto(dto);
                    employeeService.save(employee);
                    
                    results.add(Map.of(
                        "row", i + 1,
                        "status", "success",
                        "message", "Employee imported successfully",
                        "employeeId", employee.getId()
                    ));
                    successCount++;
                    
                } catch (Exception e) {
                    results.add(Map.of(
                        "row", i + 1,
                        "status", "error",
                        "message", "Error importing row: " + e.getMessage()
                    ));
                    errorCount++;
                }
            }
            
            workbook.close();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Import completed",
                "successCount", successCount,
                "errorCount", errorCount,
                "results", results
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error importing file: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/next-employee-id")
    public ResponseEntity<Map<String, Object>> getNextEmployeeId() {
        String nextId = employeeService.generateNextEmployeeId();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "nextEmployeeId", nextId
        ));
    }
    
    @GetMapping("/check-employee-id/{employeeId}")
    public ResponseEntity<Map<String, Object>> checkEmployeeId(@PathVariable String employeeId) {
        boolean exists = employeeService.existsByEmployeeId(employeeId);
        return ResponseEntity.ok(Map.of(
            "exists", exists,
            "message", exists ? "Employee ID already exists" : "Employee ID is available"
        ));
    }
    
    private String getCellValue(org.apache.poi.ss.usermodel.Row row, int cellIndex) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteWithCleanup(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateEmployee(@PathVariable Long id) {
        try {
            employeeService.deactivate(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee deactivated"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkDelete(@RequestBody List<Long> ids) {
        try {
            ids.forEach(employeeService::deleteWithCleanup);
            return ResponseEntity.ok(Map.of("success", true, "message", ids.size() + " employees deleted"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadEmployeeImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Please select a file to upload"
                ));
            }

            // Validate file type (only images)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Only image files are allowed"
                ));
            }

            // Save file (in real app, save to cloud storage or file system)
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String imageUrl = "/uploads/employees/" + fileName;
            
            // For now, save to local uploads directory
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/employees");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            java.nio.file.Files.copy(file.getInputStream(), uploadPath.resolve(fileName));

            // Update employee with image URL
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            employee.setProfileImageUrl(imageUrl);
            EmployeeDto dto = employeeMapper.toDto(employee);
            employeeService.update(id, dto);

            return ResponseEntity.ok(Map.of(
                    "message", "Image uploaded successfully",
                    "imageUrl", imageUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to upload image: " + e.getMessage()
            ));
        }
    }
}
