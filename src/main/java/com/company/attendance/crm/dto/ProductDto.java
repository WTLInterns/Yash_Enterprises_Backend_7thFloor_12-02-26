package com.company.attendance.crm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Getter
@Setter
public class ProductDto {
    private Long id;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String code;
    private String description;
    private Long categoryId;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;
    
    private Boolean active = true;
    
    private String customFields;
    
    // Audit fields (read-only in frontend)
    private Instant createdAt;
    private Instant updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private String createdByName;
    private String updatedByName;
    private String ownerName;
    private String categoryName;
}
