package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.DealStageHistory;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DealStageHistoryRepository extends JpaRepository<DealStageHistory, Long> {
    Page<DealStageHistory> findByDealOrderByChangedAtDesc(Deal deal, Pageable pageable);
    
    List<DealStageHistory> findByDeal(Deal deal);
}
