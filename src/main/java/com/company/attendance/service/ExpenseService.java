package com.company.attendance.service;
import com.company.attendance.entity.Expense;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.ExpenseRepository;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
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
    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;

    public Expense save(Expense expense) {
        // Debug logging
        System.out.println("🔥 [EXPENSE SERVICE] Saving expense: " + expense);
        System.out.println("🔥 [EXPENSE SERVICE] Employee ID: " + expense.getEmployeeId());
        System.out.println("🔥 [EXPENSE SERVICE] Employee Name: " + expense.getEmployeeName());
        System.out.println("🔥 [EXPENSE SERVICE] Department: " + expense.getDepartmentName());
        
        // Auto-fill missing fields
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
            System.out.println("🔥 [EXPENSE SERVICE] Auto-filled date: " + expense.getExpenseDate());
        }
        if (expense.getExpenseTime() == null) {
            expense.setExpenseTime(LocalTime.now());
            System.out.println("🔥 [EXPENSE SERVICE] Auto-filled time: " + expense.getExpenseTime());
        }
        if (expense.getStatus() == null) {
            expense.setStatus("PENDING");
            System.out.println("🔥 [EXPENSE SERVICE] Auto-filled status: " + expense.getStatus());
        }
        
        // Auto-fill employee name and department if employeeId is provided
        if (expense.getEmployeeId() != null && 
            (expense.getEmployeeName() == null || expense.getEmployeeName().isEmpty() ||
             expense.getDepartmentName() == null || expense.getDepartmentName().isEmpty())) {
            
            System.out.println("🔥 [EXPENSE SERVICE] Looking up employee for ID: " + expense.getEmployeeId());
            Optional<Employee> employee = employeeRepository.findById(expense.getEmployeeId());
            if (employee.isPresent()) {
                Employee emp = employee.get();
                String fullName = emp.getFirstName() + " " + emp.getLastName();
                String deptName = emp.getDepartment() != null ? emp.getDepartment().getName() : null;
                System.out.println("🔥 [EXPENSE SERVICE] Found employee: " + fullName + ", Dept: " + deptName);
                
                if (expense.getEmployeeName() == null || expense.getEmployeeName().isEmpty()) {
                    expense.setEmployeeName(fullName);
                    System.out.println("🔥 [EXPENSE SERVICE] Auto-filled employee name: " + expense.getEmployeeName());
                }
                if (expense.getDepartmentName() == null || expense.getDepartmentName().isEmpty()) {
                    expense.setDepartmentName(deptName);
                    System.out.println("🔥 [EXPENSE SERVICE] Auto-filled department: " + expense.getDepartmentName());
                }
            } else {
                System.out.println("🔥 [EXPENSE SERVICE] Employee not found for ID: " + expense.getEmployeeId());
            }
        }
        
        Expense saved = expenseRepository.save(expense);
        System.out.println("🔥 [EXPENSE SERVICE] Saved expense with ID: " + saved.getId());
        return saved;
    }

    public List<Expense> findAll() {
        List<Expense> expenses = expenseRepository.findAllWithEmployee();
        // Populate employee details for each expense
        for (Expense expense : expenses) {
            if (expense.getEmployee() != null) {
                String fullName = expense.getEmployee().getFirstName() + " " + expense.getEmployee().getLastName();
                expense.setEmployeeName(fullName);
                
                // Try to get department from multiple sources
                String deptName = null;
                if (expense.getEmployee().getDepartment() != null) {
                    deptName = expense.getEmployee().getDepartment().getName();
                } else if (expense.getEmployee().getDepartmentName() != null && !expense.getEmployee().getDepartmentName().isEmpty()) {
                    deptName = expense.getEmployee().getDepartmentName();
                } else if (expense.getEmployee().getTl() != null && expense.getEmployee().getTl().getDepartmentName() != null) {
                    deptName = expense.getEmployee().getTl().getDepartmentName();
                }
                expense.setDepartmentName(deptName);
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

    // 🔥 NEW: Find expenses by clientId for CRM integration
    public List<Expense> findByClientId(Long clientId) {
        return expenseRepository.findByClientId(clientId);
    }

    // 🔥 NEW: Get total expenses by clientId for CRM accounting
    public BigDecimal getTotalByClientId(Long clientId) {
        List<Expense> expenses = findByClientId(clientId);
        return expenses.stream()
            .map(Expense::getAmount)
            .filter(Objects::nonNull)
            .map(BigDecimal::valueOf)          // Double → BigDecimal
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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

