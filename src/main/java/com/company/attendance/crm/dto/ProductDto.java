package com.company.attendance.crm.dto;

import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class ProductDto {
    private UUID id;
    private String productName;
    private String productCode;
    private String description;
    private String productCategory;
    private Double unitPrice;
    private UUID categoryId;
    private boolean active;
    
    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private UUID ownerId;
    
    // Owner names
    private String createdByName;
    private String updatedByName;
    private String ownerName;
    
    // Custom fields
    private Map<String, Object> customFields;
}
