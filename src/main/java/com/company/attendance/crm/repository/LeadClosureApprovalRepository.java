package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.LeadClosureApproval;
import com.company.attendance.crm.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadClosureApprovalRepository extends JpaRepository<LeadClosureApproval, Long> {

    // Find all pending approvals ordered by creation date (newest first)
    List<LeadClosureApproval> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);

    // Check if approval already exists for a deal with pending status
    boolean existsByDealIdAndStatus(Long dealId, ApprovalStatus status);

    // Find approvals requested by a specific user
    List<LeadClosureApproval> findByRequestedByOrderByCreatedAtDesc(Long requestedBy);

    // Find pending approval for a specific deal
    Optional<LeadClosureApproval> findByDealIdAndStatus(Long dealId, ApprovalStatus status);

    // Find approval by ID with deal information
    @Query("SELECT a FROM LeadClosureApproval a WHERE a.id = :approvalId")
    Optional<LeadClosureApproval> findByIdWithDeal(Long approvalId);
}
