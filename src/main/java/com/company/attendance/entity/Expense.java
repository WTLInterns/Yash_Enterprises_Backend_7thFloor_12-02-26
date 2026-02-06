package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private Double amount;
    private String category;
    private String description;
    private LocalDate expenseDate;
    private String receiptUrl;
    private Long approvedBy;
    private String status;
}
