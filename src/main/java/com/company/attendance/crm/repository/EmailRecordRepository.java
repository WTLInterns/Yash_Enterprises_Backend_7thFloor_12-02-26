package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.EmailRecord;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EmailRecordRepository extends JpaRepository<EmailRecord, UUID> {
    List<EmailRecord> findByDealOrderBySentAtDesc(Deal deal);
}
