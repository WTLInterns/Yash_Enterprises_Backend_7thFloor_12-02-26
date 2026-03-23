package com.company.attendance.controller;
import com.company.attendance.dto.ExpenseDto;
import com.company.attendance.entity.Expense;
import com.company.attendance.mapper.ExpenseMapper;
import com.company.attendance.service.ExpenseService;
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
        
        Expense expense = expenseService.save(expenseMapper.toEntity(dto));
        
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
    public ResponseEntity<ExpenseDto> rejectExpense(@PathVariable Long id) {
        Expense expense = expenseService.getById(id);
        expense.setStatus("REJECTED");
        // expense.setApprovedBy(getCurrentUserId());
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
}

