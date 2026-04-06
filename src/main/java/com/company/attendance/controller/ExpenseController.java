package com.company.attendance.controller;
import com.company.attendance.dto.ExpenseDto;
import com.company.attendance.entity.Expense;
import com.company.attendance.mapper.ExpenseMapper;
import com.company.attendance.service.ExpenseService;
import com.company.attendance.crm.service.AuditService;
import com.company.attendance.util.FileUploadUtil;
import com.company.attendance.util.UploadUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;
    private final UploadUtil uploadUtil;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> listExpenses(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartmentHeader
    ) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (month != null && year != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        List<Expense> expenses;
        
        // Role-based filtering
        if (userRoleHeader != null) {
            String role = userRoleHeader;
            String department = userDepartmentHeader;
            
            if (role.equals("ADMIN") || role.equals("MANAGER") || "ACCOUNT".equals(department)) {
                // Admin / Manager / Account see all
                expenses = (employeeId != null || status != null || startDate != null || endDate != null)
                        ? expenseService.findFiltered(employeeId, status, startDate, endDate)
                        : expenseService.findAll();
            } else if (role.equals("TL") && department != null) {
                // TL sees only their department
                expenses = expenseService.findByDepartment(department);
            } else if (userIdHeader != null) {
                // Employee sees only their own expenses
                Long currentUserId = Long.valueOf(userIdHeader);
                expenses = expenseService.findByEmployeeId(currentUserId);
            } else {
                // Fallback - no filtering
                expenses = (employeeId != null || status != null || startDate != null || endDate != null)
                        ? expenseService.findFiltered(employeeId, status, startDate, endDate)
                        : expenseService.findAll();
            }
        } else {
            // No role header - fallback to original logic
            expenses = (employeeId != null || status != null || startDate != null || endDate != null)
                    ? expenseService.findFiltered(employeeId, status, startDate, endDate)
                    : expenseService.findAll();
        }
        
        var dtos = expenses.stream().map(expenseMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    // 🔥 NEW: Get expenses by clientId for CRM integration
    @GetMapping(params = "clientId")
    public ResponseEntity<List<ExpenseDto>> getExpensesByClient(@RequestParam Long clientId) {
        List<Expense> expenses = expenseService.findByClientId(clientId);
        var dtos = expenses.stream().map(expenseMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable Long id) {
        return expenseService.findById(id)
                .map(expenseMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ExpenseDto> createExpense(
            @RequestPart("expense") String expenseJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartmentHeader
    ) throws IOException {
        
        ExpenseDto dto = objectMapper.readValue(expenseJson, ExpenseDto.class);
        
        // Security fix: Force employeeId to current user for non-admin roles
        if (userIdHeader != null && userRoleHeader != null) {
            String role = userRoleHeader;
            if (!role.equals("ADMIN") && !role.equals("MANAGER") && !"ACCOUNT".equals(userDepartmentHeader)) {
                // For TL and EMPLOYEE roles, force employeeId to current user
                dto.setEmployeeId(Long.valueOf(userIdHeader));
            }
        }
        
        // 🔥 DEBUG: Check clientId
        System.out.println("CLIENT ID: " + dto.getClientId());
        
        Expense expense = expenseMapper.toEntity(dto);
        
        // 🔥 IMPORTANT: Set clientId from DTO
        expense.setClientId(dto.getClientId());
        expense.setClientName(dto.getClientName());
        
        expense = expenseService.save(expense);
        
        // 🔥 NEW: Log expense to timeline if clientId exists
        if (dto.getClientId() != null) {
            auditService.logActivity(
                dto.getClientId(),
                "EXPENSE_ADDED",
                "Expense added: ₹" + dto.getAmount() + " for " + dto.getCategory(),
                dto.getEmployeeId()
            );
        }
        
        if(file != null && !file.isEmpty()){
            String receiptUrl = uploadUtil.saveFile(file, "expenses");
            expense.setReceiptUrl(receiptUrl);
            expense = expenseService.save(expense);
        }
        
        return ResponseEntity.ok(expenseMapper.toDto(expense));
    }
    @PutMapping(path = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable Long id,
            @RequestPart("expense") String expenseJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        
        ExpenseDto dto = objectMapper.readValue(expenseJson, ExpenseDto.class);
        Expense expense = expenseMapper.toEntity(dto);
        Expense updated = expenseService.update(id, expense);
        
        if(file != null && !file.isEmpty()){
            String receiptUrl = uploadUtil.saveFile(file, "expenses");
            updated.setReceiptUrl(receiptUrl);
            updated = expenseService.save(updated);
        }
        
        return ResponseEntity.ok(expenseMapper.toDto(updated));
    }

    @PostMapping("/{id}/evidence")
    public ResponseEntity<ExpenseDto> uploadEvidence(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Create upload directory if it doesn't exist
        String uploadDir = "C:/uploads/expenses/";
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate filename using timestamp (no UUID)
        long timestamp = System.currentTimeMillis();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String fileName = timestamp + "_" + id + extension;
        
        Path filePath = uploadPath.resolve(fileName);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Store relative URL in database
        String receiptUrl = "/uploads/expenses/" + fileName;

        Expense expense = expenseService.getById(id);
        expense.setReceiptUrl(receiptUrl);
        Expense saved = expenseService.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ExpenseDto> approveExpense(@PathVariable Long id) {
        Expense expense = expenseService.getById(id);
        expense.setStatus("APPROVED");
        // You might want to set approvedBy from current user context
        // expense.setApprovedBy(getCurrentUserId());
        Expense saved = expenseService.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ExpenseDto> rejectExpense(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        Expense expense = expenseService.getById(id);
        expense.setStatus("REJECTED");
        if (body != null && body.get("reason") != null && !body.get("reason").isBlank()) {
            expense.setRejectionReason(body.get("reason"));
        }
        Expense saved = expenseService.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<ExpenseDto> markAsPaid(@PathVariable Long id) {
        Expense expense = expenseService.getById(id);
        expense.setStatus("PAID");
        // expense.setApprovedBy(getCurrentUserId());
        Expense saved = expenseService.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<java.util.Map<String, Object>> bulkUpload(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            org.apache.poi.ss.usermodel.Workbook workbook =
                new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream());
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int count = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Expense e = new Expense();
                    e.setEmployeeName(getCellStr(row, 0));
                    e.setClientName(getCellStr(row, 1));
                    e.setDepartmentName(getCellStr(row, 2));
                    e.setCategory(getCellStr(row, 3));
                    e.setDescription(getCellStr(row, 4));
                    String amtStr = getCellStr(row, 5);
                    if (amtStr != null && !amtStr.isEmpty()) e.setAmount(Double.parseDouble(amtStr));
                    String dateStr = getCellStr(row, 6);
                    if (dateStr != null && !dateStr.isEmpty()) e.setExpenseDate(java.time.LocalDate.parse(dateStr));
                    String status = getCellStr(row, 7);
                    e.setStatus(status != null && !status.isEmpty() ? status.toUpperCase() : "PENDING");
                    expenseService.save(e);
                    count++;
                } catch (Exception ex) {
                    // skip bad rows
                }
            }
            workbook.close();
            return ResponseEntity.ok(java.util.Map.of("success", true, "count", count));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(java.util.Map.of("success", false, "message", ex.getMessage()));
        }
    }

    private String getCellStr(org.apache.poi.ss.usermodel.Row row, int idx) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(idx);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}

