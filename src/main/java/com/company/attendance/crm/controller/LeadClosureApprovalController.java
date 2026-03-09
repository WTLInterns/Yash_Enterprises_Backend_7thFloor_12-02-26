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
     * Request closure approval - ACCOUNT department users only
     */
    @PostMapping("/deals/{dealId}/request-close")
    public ResponseEntity<?> requestClosure(
            @PathVariable Long dealId,
            @RequestBody LeadClosureApprovalDto.RequestApprovalRequest request) {
        
        try {
            Employee currentUser = getCurrentUser();
            
            // Check if user is from ACCOUNT department
            if (!"ACCOUNT".equals(currentUser.getDepartmentName())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Only ACCOUNT department users can request deal closure"
                ));
            }

            LeadClosureApprovalDto approval = approvalService.requestClosure(
                    dealId, 
                    request.getStage(), 
                    currentUser.getId()
            );

            return ResponseEntity.ok(Map.of(
                "message", "Closure request submitted successfully",
                "approval", approval
            ));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Closure request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to submit closure request", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to submit closure request"
            ));
        }
    }

    /**
     * Get pending approvals - MANAGER role only
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
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
     * Approve closure request - MANAGER role only
     */
    @PostMapping("/{approvalId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> approveClosure(@PathVariable Long approvalId) {
        try {
            Employee currentUser = getCurrentUser();
            
            LeadClosureApprovalDto approval = approvalService.approveClosure(
                    approvalId, 
                    currentUser.getId()
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Closure approved successfully",
                "approval", approval
            ));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Closure approval failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to approve closure {}", approvalId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to approve closure"
            ));
        }
    }

    /**
     * Reject closure request - MANAGER role only
     */
    @PostMapping("/{approvalId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
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
                "message", "Closure rejected successfully",
                "approval", approval
            ));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Closure rejection failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to reject closure {}", approvalId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to reject closure"
            ));
        }
    }

    /**
     * Get user's own approval requests
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
