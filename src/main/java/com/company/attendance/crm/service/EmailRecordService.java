package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.EmailRecord;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.EmailRecordRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmailRecordService {
    private final DealRepository dealRepository;
    private final EmailRecordRepository emailRecordRepository;

    public EmailRecordService(DealRepository dealRepository, EmailRecordRepository emailRecordRepository) {
        this.dealRepository = dealRepository;
        this.emailRecordRepository = emailRecordRepository;
    }

    public List<EmailRecord> list(UUID dealId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        return emailRecordRepository.findByDealOrderBySentAtDesc(deal);
    }

    public EmailRecord send(UUID dealId, EmailRecord record, UUID userId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        record.setDeal(deal);
        record.setSentBy(userId);
        record.setSentAt(OffsetDateTime.now());
        return emailRecordRepository.save(record);
    }
}
