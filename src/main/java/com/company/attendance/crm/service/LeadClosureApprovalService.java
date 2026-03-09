package com.company.attendance.crm.service;

import com.company.attendance.crm.dto.LeadClosureApprovalDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.LeadClosureApproval;
import com.company.attendance.crm.enums.ApprovalStatus;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.LeadClosureApprovalRepository;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadClosureApprovalService {

    private final LeadClosureApprovalRepository approvalRepository;
    private final DealRepository dealRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    /**
     * Request closure approval from ACCOUNT department user
     */
    public LeadClosureApprovalDto requestClosure(Long dealId, String stage, Long employeeId) {
        log.info("Requesting closure approval for deal {} by employee {}", dealId, employeeId);

        // Check if pending approval already exists
        if (approvalRepository.existsByDealIdAndStatus(dealId, ApprovalStatus.PENDING)) {
            throw new IllegalStateException("Approval request already pending for this deal");
        }

        // Validate stage - accept both variations
        boolean isValidStage = "CLOSE_WON".equals(stage) || "CLOSE_LOST".equals(stage) 
                           || "CLOSE_WIN".equals(stage) || "CLOSE_LOST".equals(stage);
        
        if (!isValidStage) {
            throw new IllegalArgumentException("Invalid stage. Only CLOSE_WON, CLOSE_WIN, or CLOSE_LOST are allowed");
        }
        
        // Keep original stage format - don't normalize
        // This ensures consistency between frontend and backend

        // Get deal information
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found with id: " + dealId));

        // Create approval request
        LeadClosureApproval approval = new LeadClosureApproval();
        approval.setDealId(dealId);
        approval.setRequestedBy(employeeId);
        approval.setRequestedStage(stage);
        approval.setStatus(ApprovalStatus.PENDING);

        approval = approvalRepository.save(approval);
        log.info("Created closure approval request: {}", approval.getId());

        // Send notification to MANAGER role
        sendClosureRequestNotification(approval, deal);

        return convertToDto(approval, deal);
    }

    /**
     * Approve closure request by MANAGER
     */
    public LeadClosureApprovalDto approveClosure(Long approvalId, Long managerId) {
        log.info("🔥 [APPROVAL] approveClosure called:");
        log.info("🔥 [APPROVAL] - ApprovalId: {}", approvalId);
        log.info("🔥 [APPROVAL] - ManagerId: {}", managerId);
        
        log.info("Approving closure request {} by manager {}", approvalId, managerId);

        LeadClosureApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval request is not pending");
        }

        // Update approval
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setApprovedBy(managerId);
        approval.setApprovedAt(LocalDateTime.now());

        approval = approvalRepository.save(approval);

        // Update deal stage
        Deal deal = dealRepository.findById(approval.getDealId())
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));

        deal.setStageCode(approval.getRequestedStage());
        dealRepository.save(deal);
        log.info("Updated deal {} stage to {}", deal.getId(), approval.getRequestedStage());

        // Send notification to ACCOUNT user
        sendApprovalNotification(approval, deal, true);

        return convertToDto(approval, deal);
    }

    /**
     * Reject closure request by MANAGER
     */
    public LeadClosureApprovalDto rejectClosure(Long approvalId, Long managerId, String reason) {
        log.info("Rejecting closure request {} by manager {}", approvalId, managerId);

        LeadClosureApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval request is not pending");
        }

        // Update approval
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setApprovedBy(managerId);
        approval.setApprovedAt(LocalDateTime.now());
        approval.setRejectionReason(reason);

        approval = approvalRepository.save(approval);

        // Get deal for notification
        Deal deal = dealRepository.findById(approval.getDealId())
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));

        // Send notification to ACCOUNT user
        sendApprovalNotification(approval, deal, false);

        return convertToDto(approval, deal);
    }

    /**
     * Get all pending approvals for MANAGER
     */
    @Transactional(readOnly = true)
    public List<LeadClosureApprovalDto> getPendingApprovals() {
        List<LeadClosureApproval> approvals = approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING);
        
        return approvals.stream()
                .map(approval -> {
                    Deal deal = dealRepository.findById(approval.getDealId()).orElse(null);
                    return convertToDto(approval, deal);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get approvals requested by a specific user
     */
    @Transactional(readOnly = true)
    public List<LeadClosureApprovalDto> getRequestsByUser(Long userId) {
        List<LeadClosureApproval> approvals = approvalRepository.findByRequestedByOrderByCreatedAtDesc(userId);
        
        return approvals.stream()
                .map(approval -> {
                    Deal deal = dealRepository.findById(approval.getDealId()).orElse(null);
                    return convertToDto(approval, deal);
                })
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private LeadClosureApprovalDto convertToDto(LeadClosureApproval approval, Deal deal) {
        LeadClosureApprovalDto dto = new LeadClosureApprovalDto();
        dto.setId(approval.getId());
        dto.setDealId(approval.getDealId());
        dto.setRequestedBy(approval.getRequestedBy());
        dto.setRequestedStage(approval.getRequestedStage());
        dto.setStatus(approval.getStatus().name());
        dto.setApprovedBy(approval.getApprovedBy());
        dto.setApprovedAt(approval.getApprovedAt());
        dto.setRejectionReason(approval.getRejectionReason());
        dto.setCreatedAt(approval.getCreatedAt());

        // Set deal information
        if (deal != null) {
            dto.setDealName(deal.getName());
            dto.setClientName("Client #" + deal.getClientId());
            dto.setDealValue(deal.getValueAmount() != null ? deal.getValueAmount().toString() : "0");
        }

        // Set employee names
        Employee requestedByEmployee = employeeRepository.findById(approval.getRequestedBy()).orElse(null);
        if (requestedByEmployee != null) {
            dto.setRequestedByName(requestedByEmployee.getFullName());
        }

        if (approval.getApprovedBy() != null) {
            Employee approvedByEmployee = employeeRepository.findById(approval.getApprovedBy()).orElse(null);
            if (approvedByEmployee != null) {
                dto.setApprovedByName(approvedByEmployee.getFullName());
            }
        }

        return dto;
    }

    /**
     * Send notification to MANAGER role about closure request
     */
    private void sendClosureRequestNotification(LeadClosureApproval approval, Deal deal) {
        try {
            String title = "Deal Closure Request";
            String body = String.format("Account department requested %s approval for deal: %s", 
                    approval.getRequestedStage().replace("_", " "), deal.getName());

            log.info("🔥 [NOTIFICATION] Sending closure request notification:");
            log.info("🔥 [NOTIFICATION] - Title: {}", title);
            log.info("🔥 [NOTIFICATION] - Body: {}", body);
            log.info("🔥 [NOTIFICATION] - Stage: {}", approval.getRequestedStage());
            log.info("🔥 [NOTIFICATION] - Deal: {}", deal.getName());
            log.info("🔥 [NOTIFICATION] - Approval ID: {}", approval.getId());

            notificationService.sendRoleBasedNotification("MANAGER", title, body, 
                    "DEAL_CLOSURE_REQUEST", approval.getId());

            log.info("✅ [NOTIFICATION] Closure request notification sent to MANAGER role");
        } catch (Exception e) {
            log.error("❌ [NOTIFICATION] Failed to send closure request notification", e);
        }
    }

    /**
     * Send notification to ACCOUNT user about approval decision
     */
    private void sendApprovalNotification(LeadClosureApproval approval, Deal deal, boolean approved) {
        try {
            String title = approved ? "Deal Closure Approved" : "Deal Closure Rejected";
            String body = approved
                    ? String.format("Your %s request for deal '%s' has been approved", 
                            approval.getRequestedStage().replace("_", " "), deal.getName())
                    : String.format("Your %s request for deal '%s' has been rejected. Reason: %s", 
                            approval.getRequestedStage().replace("_", " "), deal.getName(), 
                            approval.getRejectionReason() != null ? approval.getRejectionReason() : "No reason provided");

            log.info("🔥 [APPROVAL NOTIFICATION] Sending approval decision notification:");
            log.info("🔥 [APPROVAL NOTIFICATION] - Approved: {}", approved);
            log.info("🔥 [APPROVAL NOTIFICATION] - Title: {}", title);
            log.info("🔥 [APPROVAL NOTIFICATION] - Body: {}", body);
            log.info("🔥 [APPROVAL NOTIFICATION] - Deal: {}", deal.getName());
            log.info("🔥 [APPROVAL NOTIFICATION] - Stage: {}", approval.getRequestedStage());
            log.info("🔥 [APPROVAL NOTIFICATION] - Approval ID: {}", approval.getId());
            log.info("🔥 [APPROVAL NOTIFICATION] - Requested By: {}", approval.getRequestedBy());

            // 🎯 SOLUTION: Send notification directly to the employee who requested the approval
            // AND also try department notification for ACCOUNT department
            Long requestedByEmployeeId = approval.getRequestedBy();
            if (requestedByEmployeeId != null) {
                log.info("🔥 [APPROVAL NOTIFICATION] Sending direct notification to employee: {}", requestedByEmployeeId);
                notificationService.notifyEmployeeWeb(requestedByEmployeeId, title, body, 
                    "DEAL_CLOSURE_DECISION", "DEAL_CLOSURE_DECISION", approval.getId(), null);
                notificationService.notifyEmployeeMobile(requestedByEmployeeId, title, body, 
                    "DEAL_CLOSURE_DECISION", "DEAL_CLOSURE_DECISION", approval.getId(), null);
            }

            // Also try department notification (in case ACCOUNT department exists in future)
            notificationService.sendDepartmentNotification("ACCOUNT", title, body,
                    "DEAL_CLOSURE_DECISION", approval.getId());

            log.info("✅ [APPROVAL NOTIFICATION] Closure decision notification sent to employee {} and ACCOUNT department", requestedByEmployeeId);
        } catch (Exception e) {
            log.error("❌ [APPROVAL NOTIFICATION] Failed to send closure decision notification", e);
        }
    }
}
