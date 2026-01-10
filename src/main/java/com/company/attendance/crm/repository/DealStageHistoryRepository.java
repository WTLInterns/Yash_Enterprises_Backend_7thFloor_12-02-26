package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.DealStageHistory;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.List;

public interface DealStageHistoryRepository extends JpaRepository<DealStageHistory, UUID> {
    Page<DealStageHistory> findByDealOrderByChangedAtDesc(Deal deal, Pageable pageable);

    @Query(value = "select * from deal_stage_history where substring(deal_id,1,16) = uuid_to_bin(:dealId) order by changed_at desc",
            nativeQuery = true)
    List<DealStageHistory> findByDealIdCompatOrderByChangedAtDesc(@Param("dealId") String dealId);
}
