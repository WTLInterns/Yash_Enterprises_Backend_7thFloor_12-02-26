package com.company.attendance.crm.service;

import com.company.attendance.crm.dto.LeadClosureApprovalDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.LeadClosureApproval;
import com.company.attendance.crm.entity.DealStageHistory;
import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.LeadClosureApprovalRepository;
import com.company.attendance.crm.repository.DealStageHistoryRepository;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.Client;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadClosureApprovalService {

    private final LeadClosureApprovalRepository approvalRepository;
    private final DealRepository dealRepository;
    private final DealStageHistoryRepository stageHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;
    private final CrmMapper mapper;

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String getEmployeeName(Long employeeId) {
        if (employeeId == null) return "Unknown User";
        return employeeRepository.findById(employeeId)
            .map(e -> {
                String first = e.getFirstName() != null ? e.getFirstName().trim() : "";
                String last  = e.getLastName()  != null ? e.getLastName().trim()  : "";
                String full  = (first + " " + last).trim();
                return full.isEmpty() ? "User #" + employeeId : full;
            })
            .orElse("User #" + employeeId);
    }

    private String getEmployeeDept(Long employeeId) {
        if (employeeId == null) return null;
        return employeeRepository.findById(employeeId)
            .map(Employee::getDepartmentName)
            .orElse(null);
    }

    // ✅ NEW: Get client name by clientId
    private String getClientName(Long clientId) {
        if (clientId == null) return null;
        return clientRepository.findById(clientId)
            .map(Client::getName)
            .orElse(null);
    }

    // ── Request Closure ──────────────────────────────────────────────────────

    @Transactional
    public LeadClosureApprovalDto requestClosure(Long dealId, String stage, Long requestedByUserId) {
        Deal deal = dealRepository.findByIdSafe(dealId);

        boolean alreadyPending = approvalRepository.existsByDealIdAndStatus(dealId, "PENDING");
        if (alreadyPending) {
            throw new IllegalStateException("Approval request already pending for this deal");
        }

        String requesterName = getEmployeeName(requestedByUserId);
        String requesterDept = getEmployeeDept(requestedByUserId);
        // ✅ Fetch client name
        String clientName = getClientName(deal.getClientId());

        LeadClosureApproval approval = new LeadClosureApproval();
        approval.setDealId(dealId);
        approval.setDealName(deal.getName());
        approval.setClientId(deal.getClientId());
        approval.setClientName(clientName);  // ✅ Set client name
        approval.setRequestedStage(stage != null ? stage : "ACCOUNT");
        approval.setCurrentStage(deal.getStageCode());
        approval.setCurrentDepartment(deal.getDepartment());
        approval.setFromDepartment(deal.getDepartment());
        approval.setRequestedByUserId(requestedByUserId);
        approval.setRequestedByName(requesterName);
        approval.setRequestedAt(OffsetDateTime.now());
        approval.setStatus("PENDING");
        approval.setValueAmount(deal.getValueAmount());

        LeadClosureApproval saved = approvalRepository.save(approval);

        String notifTitle = "Deal Transfer Request";
        String notifMessage = requesterName
            + " (" + (requesterDept != null ? requesterDept : "Unknown") + ")"
            + " requests to transfer deal '" + deal.getName() + "'"
            + (clientName != null ? " (Client: " + clientName + ")" : "")
            + (deal.getValueAmount() != null ? " (₹" + deal.getValueAmount() + ")" : "")
            + " to Accounts. Approval required.";

        try {
            notificationService.sendRoleBasedNotification(
                "MANAGER", notifTitle, notifMessage, "DEAL_TRANSFER_REQUEST", dealId);
            log.info("✅ MANAGER notified for deal {} transfer request", dealId);
        } catch (Exception e) {
            log.error("Failed to notify MANAGER: {}", e.getMessage());
        }

        try {
            notificationService.sendRoleBasedNotification(
                "ADMIN", notifTitle, notifMessage, "DEAL_TRANSFER_REQUEST", dealId);
            log.info("✅ ADMIN notified for deal {} transfer request", dealId);
        } catch (Exception e) {
            log.error("Failed to notify ADMIN: {}", e.getMessage());
        }

        return toDto(saved, deal);
    }

    // ── Approve ──────────────────────────────────────────────────────────────

    @Transactional
    public LeadClosureApprovalDto approveClosure(Long approvalId, Long approvedByUserId) {
        LeadClosureApproval approval = approvalRepository.findById(approvalId)
            .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + approvalId));

        if (!"PENDING".equals(approval.getStatus())) {
            throw new IllegalStateException("Approval is not in PENDING state");
        }

        // ✅ approvedByUserId is the ACTUAL logged-in manager/admin who clicked approve
        String approverName = getEmployeeName(approvedByUserId);
        log.info("✅ approveClosure: approvalId={}, approvedByUserId={}, approverName={}",
            approvalId, approvedByUserId, approverName);

        approval.setStatus("APPROVED");
        approval.setReviewedByUserId(approvedByUserId);
        approval.setReviewedByName(approverName);  // ✅ Dynamic — whoever approved
        approval.setReviewedAt(OffsetDateTime.now());
        approvalRepository.save(approval);

        Deal deal = dealRepository.findByIdSafe(approval.getDealId());
        String prevDept  = deal.getDepartment();
        String prevStage = deal.getStageCode();

        deal.setDepartment("ACCOUNT");
        deal.setStageCode("INVENTORY");
        Deal savedDeal = dealRepository.save(deal);

        DealStageHistory h = new DealStageHistory();
        h.setDeal(savedDeal);
        h.setPreviousStage(prevStage);
        h.setNewStage("INVENTORY");
        h.setChangedBy(String.valueOf(approvedByUserId));
        h.setChangedAt(OffsetDateTime.now());
        stageHistoryRepository.save(h);

        // ✅ Notify requester with DYNAMIC approver name
        try {
            String title = "Deal Transfer Approved ✅";
            String message = "Your request to transfer deal '" + deal.getName() + "'"
                + " from " + prevDept + " to Accounts has been approved by " + approverName + ".";
            notificationService.sendUserNotification(
                approval.getRequestedByUserId(), title, message, "DEAL_APPROVED", deal.getId());
            log.info("✅ Requester {} notified of approval by {}", approval.getRequestedByUserId(), approverName);
        } catch (Exception e) {
            log.error("Failed to notify requester of approval: {}", e.getMessage());
        }

        // ✅ Notify ACCOUNT department with DYNAMIC approver name
        try {
            String clientName = getClientName(deal.getClientId());
            String title = "New Deal Received 🎉";
            String message = "Deal '" + deal.getName() + "'"
                + (clientName != null ? " (Client: " + clientName + ")" : "")
                + (deal.getValueAmount() != null ? " (₹" + deal.getValueAmount() + ")" : "")
                + " from " + prevDept + " has been transferred to Accounts."
                + " Approved by " + approverName + ".";
            notificationService.sendDepartmentNotification(
                "ACCOUNT", title, message, "DEAL_TRANSFER", deal.getId());
            log.info("✅ ACCOUNT dept notified of new deal");
        } catch (Exception e) {
            log.error("Failed to notify ACCOUNT dept: {}", e.getMessage());
        }

        // Notify original department
        try {
            if (prevDept != null && !prevDept.equals("ACCOUNT")) {
                String title = "Deal Transferred to Accounts ✅";
                String message = "Deal '" + deal.getName()
                    + "' has been successfully transferred to the Accounts department."
                    + " Approved by " + approverName + ".";
                notificationService.sendDepartmentNotification(
                    prevDept, title, message, "DEAL_TRANSFER", deal.getId());
            }
        } catch (Exception e) {
            log.error("Failed to notify original dept: {}", e.getMessage());
        }

        return toDto(approval, deal);
    }

    // ── Reject ───────────────────────────────────────────────────────────────

    @Transactional
    public LeadClosureApprovalDto rejectClosure(Long approvalId, Long rejectedByUserId, String reason) {
        LeadClosureApproval approval = approvalRepository.findById(approvalId)
            .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + approvalId));

        if (!"PENDING".equals(approval.getStatus())) {
            throw new IllegalStateException("Approval is not in PENDING state");
        }

        String rejectorName = getEmployeeName(rejectedByUserId);
        log.info("✅ rejectClosure: approvalId={}, rejectedByUserId={}, rejectorName={}",
            approvalId, rejectedByUserId, rejectorName);

        approval.setStatus("REJECTED");
        approval.setReviewedByUserId(rejectedByUserId);
        approval.setReviewedByName(rejectorName);  // ✅ Dynamic
        approval.setReviewedAt(OffsetDateTime.now());
        approval.setRejectionReason(reason);
        approvalRepository.save(approval);

        try {
            Deal deal = dealRepository.findByIdSafe(approval.getDealId());
            String title = "Deal Transfer Rejected ❌";
            String message = "Your request to transfer deal '" + deal.getName() + "'"
                + " to Accounts has been rejected by " + rejectorName + "."
                + (reason != null && !reason.isBlank() ? " Reason: " + reason : "");
            notificationService.sendUserNotification(
                approval.getRequestedByUserId(), title, message, "DEAL_REJECTED", deal.getId());
            log.info("✅ Requester {} notified of rejection by {}", approval.getRequestedByUserId(), rejectorName);
        } catch (Exception e) {
            log.error("Failed to notify requester of rejection: {}", e.getMessage());
        }

        Deal deal = dealRepository.findByIdSafe(approval.getDealId());
        return toDto(approval, deal);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<LeadClosureApprovalDto> getPendingApprovals() {
        return approvalRepository.findByStatusOrderByRequestedAtDesc("PENDING")
            .stream()
            .map(a -> toDto(a, dealRepository.findByIdSafe(a.getDealId())))
            .collect(Collectors.toList());
    }

    public List<LeadClosureApprovalDto> getAllApprovals() {
        return approvalRepository.findAllByOrderByRequestedAtDesc()
            .stream()
            .map(a -> toDto(a, dealRepository.findByIdSafe(a.getDealId())))
            .collect(Collectors.toList());
    }

    public List<LeadClosureApprovalDto> getRequestsByUser(Long userId) {
        return approvalRepository.findByRequestedByUserIdOrderByRequestedAtDesc(userId)
            .stream()
            .map(a -> toDto(a, dealRepository.findByIdSafe(a.getDealId())))
            .collect(Collectors.toList());
    }

    public long getPendingCount() {
        return approvalRepository.countByStatus("PENDING");
    }

    // ── DTO Mapper ───────────────────────────────────────────────────────────

    private LeadClosureApprovalDto toDto(LeadClosureApproval a, Deal deal) {
        LeadClosureApprovalDto dto = new LeadClosureApprovalDto();
        dto.setId(a.getId());
        dto.setDealId(a.getDealId());
        dto.setDealName(a.getDealName() != null ? a.getDealName()
            : (deal != null ? deal.getName() : ""));
        dto.setClientId(a.getClientId() != null ? a.getClientId()
            : (deal != null ? deal.getClientId() : null));

        // ✅ clientName: use stored value or fetch fresh
        if (a.getClientName() != null) {
            dto.setClientName(a.getClientName());
        } else if (deal != null && deal.getClientId() != null) {
            dto.setClientName(getClientName(deal.getClientId()));
        }

        dto.setRequestedStage(a.getRequestedStage());
        dto.setCurrentStage(a.getCurrentStage());
        dto.setCurrentDepartment(a.getCurrentDepartment());
        dto.setFromDepartment(a.getFromDepartment());
        dto.setRequestedByUserId(a.getRequestedByUserId());
        dto.setRequestedByName(a.getRequestedByName());
        dto.setRequestedAt(a.getRequestedAt());
        dto.setStatus(a.getStatus());
        dto.setReviewedByUserId(a.getReviewedByUserId());
        dto.setReviewedByName(a.getReviewedByName());
        dto.setReviewedAt(a.getReviewedAt());
        dto.setRejectionReason(a.getRejectionReason());
        dto.setValueAmount(a.getValueAmount() != null ? a.getValueAmount()
            : (deal != null ? deal.getValueAmount() : null));
        return dto;
    }
}
