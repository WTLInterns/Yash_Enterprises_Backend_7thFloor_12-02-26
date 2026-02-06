package com.company.attendance.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormDto {
    private Long id;
    private String name;
    private String description;
    private String schema;
    private Long clientId;
    private String clientName;

    private Long createdBy;
    private Long updatedBy;

    private String createdByName;
    private String updatedByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isActive;
}

