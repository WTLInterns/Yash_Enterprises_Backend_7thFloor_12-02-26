package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private String employeeName;

    private String departmentName;

    private Double amount;

    private String category;

    private String description;

    private LocalDate expenseDate;

    private LocalTime expenseTime;

    private String receiptUrl;

    private String status;

    // Add employee relationship for mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeId", insertable = false, updatable = false)
    private Employee employee;

    private Long approvedBy;
}
