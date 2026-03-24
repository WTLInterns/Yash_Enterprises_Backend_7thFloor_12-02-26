package com.company.attendance.crm.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadClosureApprovalDto {

    private Long id;
    private Long dealId;
    private String dealName;
    private Long clientId;
    private String clientName;
    private String requestedStage;
    private String currentStage;
    private String currentDepartment;
    private String fromDepartment;
    private Long requestedByUserId;
    private String requestedByName;
    private OffsetDateTime requestedAt;
    private String status;
    private Long reviewedByUserId;
    private String reviewedByName;
    private OffsetDateTime reviewedAt;
    private String rejectionReason;
    private java.math.BigDecimal valueAmount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestApprovalRequest {
        private String stage;
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalAction {
        private String reason;
    }
}
