package com.company.attendance.crm.dto;

import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class BankDto {
    private UUID id;
    private String bankName;
    private String branchName;
    private String address;
    private String phone;
    private String website;
    private String district;
    private String taluka;
    private String pinCode;
    private String description;
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
