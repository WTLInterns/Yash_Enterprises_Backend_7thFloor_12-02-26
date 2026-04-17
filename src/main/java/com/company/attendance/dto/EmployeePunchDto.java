package com.company.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePunchDto {
    private Long id;
    private Long sessionId;
    private Long employeeId;
    private String employeeName;
    private Long taskId;
    private String attendanceStatus;
    
    @NotBlank(message = "Punch type is required")
    private String punchType; // IN, OUT, BREAK_IN, BREAK_OUT
    private LocalDateTime punchTime;
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    private Double altitude;
    private Double accuracy;
    private String locationAddress;
    private Boolean isWithinGeofence;
    private Long geofenceId;
    private String geofenceName;
    private String deviceInfo;
    private String ipAddress;
    private String notes;
    private Boolean isManualPunch;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
