package com.company.attendance.crm.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

@Data
public class ClientDto {
    private Long id;
    
    @NotBlank(message = "Client name is required")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String contactPhone;
    private String address;
    private String notes;
    
    private String customFields;
    private Boolean active = true;
    
    // Audit fields (read-only in frontend)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant updatedAt;
    
    private Integer createdBy;
    private Integer updatedBy;
    private Integer ownerId;
    
    // Owner names for display
    private String createdByName;
    private String updatedByName;
    private String ownerName;
}
