package com.company.attendance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class AttendanceDto {
    private Long id;
    private Long employeeId;
    private LocalDate date;
    private OffsetDateTime punchInTime;
    private OffsetDateTime punchOutTime;
    private String punchInAddress;
    private String punchOutAddress;
    private BigDecimal inLocationLat;
    private BigDecimal inLocationLng;
    private BigDecimal outLocationLat;
    private BigDecimal outLocationLng;
    private BigDecimal totalHours;
    private BigDecimal totalKm;
    private String status;
    private String note;
    private String deviceInfo;
    private Boolean isLunchIn;
    private Boolean isLunchOut;

    // Explicit getters used by ReportsController (defensive if Lombok is not processed)

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}
