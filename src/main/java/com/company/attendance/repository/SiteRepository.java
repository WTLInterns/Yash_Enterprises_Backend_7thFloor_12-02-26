package com.company.attendance.repository;

import com.company.attendance.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    // JpaRepository already provides deleteAllByIdInBatch for bulk deletion.

}
