package com.company.attendance.repository;
import com.company.attendance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e FROM Expense e WHERE (:employeeId IS NULL OR e.employeeId = :employeeId) " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:startDate IS NULL OR e.expenseDate >= :startDate) " +
            "AND (:endDate IS NULL OR e.expenseDate <= :endDate)")
    List<Expense> findFiltered(
            @Param("employeeId") Long employeeId,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}


