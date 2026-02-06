package com.company.attendance.service;
import com.company.attendance.entity.Expense;
import com.company.attendance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public List<Expense> findFiltered(Long employeeId, String status, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findFiltered(employeeId, status, startDate, endDate);
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense getById(Long id) {
        return expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    public Expense update(Long id, Expense updated) {
        Expense existing = getById(id);

        if (updated.getEmployeeId() != null) existing.setEmployeeId(updated.getEmployeeId());
        if (updated.getAmount() != null) existing.setAmount(updated.getAmount());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getExpenseDate() != null) existing.setExpenseDate(updated.getExpenseDate());
        if (updated.getReceiptUrl() != null) existing.setReceiptUrl(updated.getReceiptUrl());
        if (updated.getApprovedBy() != null) existing.setApprovedBy(updated.getApprovedBy());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());

        return expenseRepository.save(existing);
    }

    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }
}

