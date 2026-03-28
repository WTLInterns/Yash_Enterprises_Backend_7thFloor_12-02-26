package com.company.attendance.crm.controller;

import com.company.attendance.crm.service.DealExcelImportService;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DealExcelUploadController {

    private final DealExcelImportService dealExcelImportService;
    private final EmployeeRepository employeeRepository;

    /**
     * Upload Excel file to create Customers + Deals in bulk
     * Department-aware: Uses logged-in user's department (unless Admin)
     */
    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartment) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only .xlsx files are supported"));
            }

            // 🔥 HEADER FALLBACK: Derive from employeeId if headers missing
            String derivedUserRole = userRole;
            String derivedUserDepartment = userDepartment;
            
            if (userRole == null || userDepartment == null) {
                if (userId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "User ID required for role/department derivation"));
                }
                
                Employee employee = employeeRepository.findByIdWithRelationships(Long.valueOf(userId))
                    .orElseThrow(() -> new RuntimeException("Employee not found for derivation"));
                
                derivedUserRole = employee.getRole() != null ? employee.getRole().getName() : null;
                derivedUserDepartment = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
            }

            // 🔥 ROLE CHECK: Only ADMIN, MANAGER, TL, EMPLOYEE can upload
            if (!Set.of("ADMIN", "MANAGER", "TL", "EMPLOYEE").contains(derivedUserRole)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied for Excel upload"));
            }

            // Admin can override department from Excel, others use their own department
            boolean allowDepartmentOverride = "ADMIN".equals(derivedUserRole);
            
            var result = dealExcelImportService.importDealsFromExcel(
                file, 
                derivedUserDepartment, 
                allowDepartmentOverride,
                userId != null ? Long.valueOf(userId) : null
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Excel upload failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Excel upload failed: " + e.getMessage()));
        }
    }

    /**
     * Download Excel template with required headers
     */
    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = dealExcelImportService.generateTemplate();
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=deal-import-template.xlsx")
                .body(template);
                
        } catch (Exception e) {
            log.error("Template download failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
