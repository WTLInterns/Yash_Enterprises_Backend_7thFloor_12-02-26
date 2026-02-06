package com.company.attendance.controller;
import com.company.attendance.dto.ExpenseDto;
import com.company.attendance.entity.Expense;
import com.company.attendance.mapper.ExpenseMapper;
import com.company.attendance.service.ExpenseService;
import com.company.attendance.util.FileUploadUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> listExpenses(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status
    ) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (month != null && year != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        var expenses = (employeeId != null || status != null || startDate != null || endDate != null)
                ? expenseService.findFiltered(employeeId, status, startDate, endDate)
                : expenseService.findAll();
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
    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody ExpenseDto dto) {
        Expense expense = expenseService.save(expenseMapper.toEntity(dto));
        return ResponseEntity.ok(expenseMapper.toDto(expense));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDto dto) {
        Expense expense = expenseMapper.toEntity(dto);
        Expense updated = expenseService.update(id, expense);
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

        Expense expense = expenseService.getById(id);
        String savedPath = FileUploadUtil.saveFile(uploadDir, file);

        // Expose through StaticResourceConfig (/uploads/**)
        String url = savedPath;
        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        expense.setReceiptUrl(url);
        Expense saved = expenseService.save(expense);
        return ResponseEntity.ok(expenseMapper.toDto(saved));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

