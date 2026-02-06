package com.company.attendance.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeTrackingDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double accuracy;
    private LocalDateTime timestamp;
    private String locationAddress;
    private String resolvedAddress;
    private String trackingType;
    private String deviceInfo;
    private String ipAddress;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
