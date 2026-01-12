package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Activity;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Page<Activity> findByDeal(Deal deal, Pageable pageable);
    Page<Activity> findByDealAndType(Deal deal, ActivityType type, Pageable pageable);
    List<Activity> findByDealOrderByCreatedAtDesc(Deal deal);
    List<Activity> findByDealAndTypeOrderByCreatedAtDesc(Deal deal, ActivityType type);
}
