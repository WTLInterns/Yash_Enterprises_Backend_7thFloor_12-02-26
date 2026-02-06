package com.company.attendance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {

    private Long id;

    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;

    private String employeeId;   // employee unique id string
    private String userId;

    private String employeeCode;

    private Long roleId;
    private String roleName;

    private Long teamId;
    private String teamName;

    private Long designationId;
    private String designationName;

    private Long reportingManagerId;
    private String reportingManagerName;

    private Long organizationId;
    private String organizationName;

    private Long departmentId;
    private String departmentName;

    private Long shiftId;
    private String shiftName;

    private String status;   // "ACTIVE" "INACTIVE"
    private Boolean attendanceAllowed;

    private String customDesignation;

    private LocalDate hiredAt;
    private LocalDate terminationDate;

    private BigDecimal locationLat;
    private BigDecimal locationLng;

    private LocalDate dateOfBirth;
    private String gender;

    private String profileImageUrl;
    private String profileImageBase64; // if you use base64 upload
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
