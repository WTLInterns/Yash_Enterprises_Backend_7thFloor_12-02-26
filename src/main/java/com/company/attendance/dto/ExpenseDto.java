package com.company.attendance.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDto {
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
    private String rejectionReason;
    
    // 🔥 NEW: Client mapping for CRM integration
    private Long clientId;
    private String clientName;
}
