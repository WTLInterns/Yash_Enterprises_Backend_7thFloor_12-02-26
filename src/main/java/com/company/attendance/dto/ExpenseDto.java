package com.company.attendance.dto;
import lombok.Data;
import java.time.LocalDate;
@Data
public class ExpenseDto {
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
