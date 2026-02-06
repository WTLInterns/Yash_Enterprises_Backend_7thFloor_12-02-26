package com.company.attendance.repository;

import com.company.attendance.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    // ✅ Find sites by client ID for CRM structure
    List<Site> findByClientId(Long clientId);

    // ✅ Check if site exists for client
    boolean existsByClientIdAndSiteId(Long clientId, String siteId);

    // ✅ Delete sites by client ID
    void deleteByClientId(Long clientId);

    // JpaRepository already provides deleteAllByIdInBatch for bulk deletion.
}
