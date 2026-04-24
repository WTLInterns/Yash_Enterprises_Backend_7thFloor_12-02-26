package com.company.attendance.service;
import com.company.attendance.entity.Expense;
import com.company.attendance.repository.ExpenseRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.crm.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final DealRepository dealRepository;
    private final ClientRepository clientRepository;

    public Expense save(Expense expense) {
        if (expense.getExpenseDate() == null) expense.setExpenseDate(LocalDate.now());
        if (expense.getExpenseTime() == null) expense.setExpenseTime(LocalTime.now());
        if (expense.getStatus() == null)      expense.setStatus("PENDING");

        // Auto-fill employeeName from employee record
        if (expense.getEmployeeId() != null &&
                (expense.getEmployeeName() == null || expense.getEmployeeName().isBlank())) {
            employeeRepository.findById(expense.getEmployeeId()).ifPresent(emp ->
                expense.setEmployeeName(emp.getFirstName() + " " + emp.getLastName()));
        }

        // DEAL is single source of truth: ALWAYS derive clientId, clientName, departmentName, stageCode from deal
        if (expense.getDealId() != null) {
            log.info("[EXPENSE SAVE] dealId={} present — deriving from deal. Before: clientId={} dept='{}' stage='{}'",
                expense.getDealId(), expense.getClientId(), expense.getDepartmentName(), expense.getStageCode());
            dealRepository.findById(expense.getDealId()).ifPresent(deal -> {
                expense.setClientId(deal.getClientId());
                expense.setDepartmentName(deal.getDepartment());
                expense.setStageCode(deal.getStageCode());
                log.info("[EXPENSE SAVE] After deal derivation: clientId={} dept='{}' stage='{}'",
                    deal.getClientId(), deal.getDepartment(), deal.getStageCode());
                // Derive clientName from client entity if not already set
                if (expense.getClientName() == null || expense.getClientName().isBlank()) {
                    clientRepository.findById(deal.getClientId()).ifPresent(client -> {
                        expense.setClientName(client.getName());
                        log.info("[EXPENSE SAVE] clientName resolved: '{}'", client.getName());
                    });
                }
            });
        } else {
            log.info("[EXPENSE SAVE] No dealId — saving as-is: clientId={} dept='{}' stage='{}'",
                expense.getClientId(), expense.getDepartmentName(), expense.getStageCode());
        }

        return expenseRepository.save(expense);
    }

    public List<Expense> findAll() {
        List<Expense> expenses = expenseRepository.findAllWithEmployee();
        // Batch-load all deals referenced by expenses to resolve current clientId
        List<Long> dealIds = expenses.stream()
            .map(Expense::getDealId).filter(Objects::nonNull).distinct().toList();
        java.util.Map<Long, com.company.attendance.crm.entity.Deal> dealsById = new java.util.HashMap<>();
        if (!dealIds.isEmpty()) {
            dealRepository.findAllById(dealIds).forEach(d -> dealsById.put(d.getId(), d));
        }
        for (Expense expense : expenses) {
            if (expense.getEmployee() != null) {
                expense.setEmployeeName(
                    expense.getEmployee().getFirstName() + " " + expense.getEmployee().getLastName());
            }
            // Always resolve clientId from deal (fixes stale clientId from deleted/re-imported clients)
            if (expense.getDealId() != null) {
                com.company.attendance.crm.entity.Deal deal = dealsById.get(expense.getDealId());
                if (deal != null && deal.getClientId() != null) {
                    expense.setClientId(deal.getClientId());
                }
            }
        }
        return expenses;
    }

    public List<Expense> findFiltered(Long employeeId, String status, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findFiltered(employeeId, status, startDate, endDate);
    }

    public List<Expense> findByDepartment(String department) {
        return expenseRepository.findByDepartment(department);
    }

    public List<Expense> findByEmployeeId(Long employeeId) {
        return expenseRepository.findByEmployeeId(employeeId);
    }

    public List<Expense> findByClientId(Long clientId) {
        return expenseRepository.findByClientId(clientId);
    }

    public List<Expense> findByDealId(Long dealId) {
        return expenseRepository.findByDealId(dealId);
    }

    public BigDecimal getTotalByClientId(Long clientId) {
        return findByClientId(clientId).stream()
            .map(Expense::getAmount)
            .filter(Objects::nonNull)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense getById(Long id) {
        return expenseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Expense not found: " + id));
    }

    public Expense update(Long id, Expense updated) {
        Expense existing = getById(id);
        if (updated.getEmployeeId()   != null) existing.setEmployeeId(updated.getEmployeeId());
        if (updated.getAmount()       != null) existing.setAmount(updated.getAmount());
        if (updated.getCategory()     != null) existing.setCategory(updated.getCategory());
        if (updated.getDescription()  != null) existing.setDescription(updated.getDescription());
        if (updated.getExpenseDate()  != null) existing.setExpenseDate(updated.getExpenseDate());
        if (updated.getReceiptUrl()   != null) existing.setReceiptUrl(updated.getReceiptUrl());
        if (updated.getApprovedBy()   != null) existing.setApprovedBy(updated.getApprovedBy());
        if (updated.getStatus()       != null) existing.setStatus(updated.getStatus());
        if (updated.getClientId()     != null) existing.setClientId(updated.getClientId());
        if (updated.getClientName()   != null) existing.setClientName(updated.getClientName());
        if (updated.getDealId()       != null) existing.setDealId(updated.getDealId());
        if (updated.getStageCode()    != null) existing.setStageCode(updated.getStageCode());
        if (updated.getExpenseType()  != null) existing.setExpenseType(updated.getExpenseType());
        // client/deal department always wins
        if (updated.getDepartmentName() != null && !updated.getDepartmentName().isBlank()) {
            existing.setDepartmentName(updated.getDepartmentName());
        }
        return expenseRepository.save(existing);
    }

    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }
}
