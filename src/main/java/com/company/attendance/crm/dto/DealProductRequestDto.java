package com.company.attendance.crm.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DealProductRequestDto {
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal tax;
}
