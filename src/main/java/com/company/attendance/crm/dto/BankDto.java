package com.company.attendance.crm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Data
@Getter
@Setter
public class BankDto {
    private Long id;
    
    @NotBlank(message = "Bank name is required")
    private String name;
    
    private String branchName;
    private String phone;
    private String website;
    private String address;
    private String district;
    private String taluka;
    private String pinCode;
    private String description;
    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String currency;
    private String taxId;
    private String registrationNumber;
    private String fiscalYearStart;
    private String fiscalYearEnd;
    private String primaryColor;
    private String secondaryColor;
    
    private String customFields;
    private Boolean active = true;
    
    // Audit fields (read-only in frontend)
    private Instant createdAt;
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;
    
    // Owner names for display
    private String createdByName;
    private String updatedByName;
    private String ownerName;
}
