package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "lead_closure_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadClosureApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_id", nullable = false)
    private Long dealId;

    @Column(name = "deal_name")
    private String dealName;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "requested_stage", nullable = false)
    private String requestedStage;

    @Column(name = "current_stage")
    private String currentStage;

    @Column(name = "current_department")
    private String currentDepartment;

    @Column(name = "from_department")
    private String fromDepartment;

    @Column(name = "requested_by_user_id", nullable = false)
    private Long requestedByUserId;

    @Column(name = "requested_by_name")
    private String requestedByName;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_by_name")
    private String reviewedByName;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "value_amount")
    private java.math.BigDecimal valueAmount;
}
