package com.company.attendance.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class InvoiceItemDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal rate;
    private BigDecimal qty;
    private BigDecimal amount;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal total;
    
    // Audit Fields
    private Instant createdAt;
    private Instant updatedAt;
}
