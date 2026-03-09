package com.company.attendance.crm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadClosureApprovalDto {

    private Long id;
    private Long dealId;
    private String dealName;
    private String clientName;
    private String dealValue;
    private Long requestedBy;
    private String requestedByName;
    private String requestedStage;
    private String status;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestApprovalRequest {
        private String stage;
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalAction {
        private String reason;
    }
}
