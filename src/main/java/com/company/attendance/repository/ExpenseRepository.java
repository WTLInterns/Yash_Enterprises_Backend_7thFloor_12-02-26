package com.company.attendance.repository;
import com.company.attendance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.employee LEFT JOIN FETCH e.employee.tl WHERE (:employeeId IS NULL OR e.employeeId = :employeeId) " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:startDate IS NULL OR e.expenseDate >= :startDate) " +
            "AND (:endDate IS NULL OR e.expenseDate <= :endDate)")
    List<Expense> findFiltered(
            @Param("employeeId") Long employeeId,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.employee LEFT JOIN FETCH e.employee.tl")
    List<Expense> findAllWithEmployee();
    
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.employee LEFT JOIN FETCH e.employee.tl WHERE e.employee.departmentName = :department")
    List<Expense> findByDepartment(@Param("department") String department);
    
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.employee LEFT JOIN FETCH e.employee.tl WHERE e.employeeId = :employeeId")
    List<Expense> findByEmployeeId(@Param("employeeId") Long employeeId);
}


