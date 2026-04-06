package com.company.attendance.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Data
public class LeaveRequestDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private String reason;

    private String approvedByName;
    private String approvalRole;
    private OffsetDateTime approvedAt;
}
