package com.company.attendance.crm.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
public class DealDto {
    private Long id;
    
    @NotBlank(message = "Deal name is required")
    private String name;
    
    @NotNull(message = "Client is required")
    private Long clientId;
    
    private Long bankId;
    private String branchName;
    private String relatedBankName;
    private String description;
    
    @Min(value = 0, message = "Value amount must be greater than or equal to 0")
    private BigDecimal valueAmount;
    
    @Min(value = 0, message = "Required amount must be greater than or equal to 0")
    private BigDecimal requiredAmount;
    
    @Min(value = 0, message = "Outstanding amount must be greater than or equal to 0")
    private BigDecimal outstandingAmount;
    
    private LocalDate closingDate;
    private String stage;
    
    private String customFields;
    private Boolean active = true;
    
    // Audit fields (read-only in frontend)
    private Instant createdAt;
    private Instant updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    
    // Owner names for display
    private String createdByName;
    private String updatedByName;
    private String ownerName;
    
    // Related entity names for display
    private String clientName;
    private String bankName;
}
