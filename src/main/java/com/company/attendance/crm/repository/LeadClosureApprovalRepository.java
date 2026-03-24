package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.LeadClosureApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadClosureApprovalRepository extends JpaRepository<LeadClosureApproval, Long> {

    // Find approvals by status ordered by requested date (newest first)
    List<LeadClosureApproval> findByStatusOrderByRequestedAtDesc(String status);

    // Check if approval already exists for a deal with specific status
    boolean existsByDealIdAndStatus(Long dealId, String status);

    // Find approvals requested by a specific user ordered by requested date
    List<LeadClosureApproval> findByRequestedByUserIdOrderByRequestedAtDesc(Long userId);

    // Find all approvals ordered by requested date (newest first)
    List<LeadClosureApproval> findAllByOrderByRequestedAtDesc();

    // Find approvals for a specific deal ordered by requested date
    List<LeadClosureApproval> findByDealIdOrderByRequestedAtDesc(Long dealId);

    // Count approvals by status
    long countByStatus(String status);

    // Find pending approval for a specific deal
    Optional<LeadClosureApproval> findByDealIdAndStatus(Long dealId, String status);
}
