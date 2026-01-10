package com.company.attendance.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CaseDto {
    private Long id;
    
    @NotBlank(message = "Case number is required")
    private String caseNumber;
    
    @NotBlank(message = "Case title is required")
    private String title;
    
    private String description;
    private String status;
    private String priority;
    private UUID clientId;
    private String clientName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
