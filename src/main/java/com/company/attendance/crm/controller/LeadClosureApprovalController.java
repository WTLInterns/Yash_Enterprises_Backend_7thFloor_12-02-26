package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.LeadClosureApprovalDto;
import com.company.attendance.crm.service.LeadClosureApprovalService;
import com.company.attendance.entity.Employee;
import com.company.attendance.crm.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LeadClosureApprovalController {

    private final LeadClosureApprovalService approvalService;
    private final AuditService auditService;

    /**
     * Request closure approval — ANY department user can request
     * (Previously restricted to ACCOUNT only — REMOVED that restriction)
     */
    @PostMapping("/deals/{dealId}/request-close")
    public ResponseEntity<?> requestClosure(
            @PathVariable Long dealId,
            @RequestBody LeadClosureApprovalDto.RequestApprovalRequest request) {

        try {
            Employee currentUser = getCurrentUser();

            // 🔥 REMOVED: ACCOUNT-only restriction
            // Any department user reaching ACCOUNT terminal stage can request transfer

            LeadClosureApprovalDto approval = approvalService.requestClosure(
                    dealId,
                    request.getStage(),
                    currentUser.getId()
            );

            return ResponseEntity.ok(Map.of(
                "message", "Transfer request submitted successfully. Waiting for Manager approval.",
                "approval", approval
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Closure request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to submit closure request", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to submit request"
            ));
        }
    }

    /**
     * Get pending approvals — MANAGER or ADMIN role
     */
    @GetMapping("/pending")
    public ResponseEntity<List<LeadClosureApprovalDto>> getPendingApprovals() {
        try {
            List<LeadClosureApprovalDto> approvals = approvalService.getPendingApprovals();
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            log.error("Failed to fetch pending approvals", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all approvals (pending + history) — MANAGER or ADMIN
     */
    @GetMapping("/all")
    public ResponseEntity<List<LeadClosureApprovalDto>> getAllApprovals() {
        try {
            List<LeadClosureApprovalDto> approvals = approvalService.getAllApprovals();
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            log.error("Failed to fetch all approvals", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Approve — MANAGER or ADMIN
     * On approve: deal moves to ACCOUNT/INVENTORY
     * Notifications sent to: requester + ACCOUNT dept
     */
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<?> approveClosure(@PathVariable Long approvalId) {
        try {
            Employee currentUser = getCurrentUser();
            
            LeadClosureApprovalDto approval = approvalService.approveClosure(
                    approvalId, 
                    currentUser.getId()
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Deal approved and transferred to Accounts successfully",
                "approval", approval
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Approval failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to approve closure {}", approvalId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to approve"));
        }
    }

    /**
     * Reject — MANAGER or ADMIN
     * On reject: deal stays in original dept/stage
     * Notification sent to: requester
     */
    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<?> rejectClosure(
            @PathVariable Long approvalId,
            @RequestBody LeadClosureApprovalDto.ApprovalAction action) {

        try {
            Employee currentUser = getCurrentUser();

            LeadClosureApprovalDto approval = approvalService.rejectClosure(
                    approvalId,
                    currentUser.getId(),
                    action.getReason()
            );

            return ResponseEntity.ok(Map.of(
                "message", "Request rejected",
                "approval", approval
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Rejection failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to reject closure {}", approvalId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to reject"));
        }
    }

    /**
     * Get current user's own requests
     */
    @GetMapping("/my-requests")
    public ResponseEntity<List<LeadClosureApprovalDto>> getMyRequests() {
        try {
            Employee currentUser = getCurrentUser();
            List<LeadClosureApprovalDto> requests = approvalService.getRequestsByUser(currentUser.getId());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Failed to fetch user requests", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get approval count (for badge on nav)
     */
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        try {
            long count = approvalService.getPendingCount();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }

    /**
     * Get current authenticated user
     */
    private Employee getCurrentUser() {
        Integer userId = auditService.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return auditService.getCurrentUser(userId);
    }
}
