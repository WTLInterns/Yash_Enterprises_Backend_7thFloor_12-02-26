package com.company.attendance.crm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class DealProductDto {
    private Integer id;
    private Long dealId;

    private Long productId;
    private String productName;

    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
