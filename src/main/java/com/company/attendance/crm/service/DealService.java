package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealStageHistory;
import com.company.attendance.crm.enums.DealStage;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.DealStageHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class DealService {
    private final DealRepository dealRepository;
    private final DealStageHistoryRepository historyRepository;

    public DealService(DealRepository dealRepository, DealStageHistoryRepository historyRepository) {
        this.dealRepository = dealRepository;
        this.historyRepository = historyRepository;
    }

    public Optional<Deal> findById(UUID id) {
        return dealRepository.findByIdSafe(id);
    }

    public Deal create(Deal deal, UUID userId) {
        deal.setCreatedBy(userId);
        deal.setCreatedAt(OffsetDateTime.now());
        return dealRepository.save(deal);
    }

    public Deal update(UUID id, Deal incoming, UUID userId) {
        Deal db = dealRepository.findByIdSafe(id).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        // copy updatable fields
        db.setName(incoming.getName());
        db.setValueAmount(incoming.getValueAmount());
        db.setClosingDate(incoming.getClosingDate());
        db.setBranchName(incoming.getBranchName());
        db.setRelatedBankName(incoming.getRelatedBankName());
        db.setDescription(incoming.getDescription());
        db.setRequiredAmount(incoming.getRequiredAmount());
        db.setOutstandingAmount(incoming.getOutstandingAmount());
        db.setOwnerId(incoming.getOwnerId());

        // stage change via dedicated method; ignore here
        db.setModifiedBy(userId);
        db.setModifiedAt(OffsetDateTime.now());
        return dealRepository.save(db);
    }

    public void delete(UUID id) {
        dealRepository.deleteById(id);
    }

    @Transactional
    public Deal changeStage(UUID dealId, DealStage newStage, String note, UUID userId) {
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        DealStage previous = deal.getStage();
        if (previous == newStage) return deal; // no-op
        // IMPORTANT:
        // This project uses a legacy UUID storage (padded BINARY(36)). When a managed Deal entity is mutated,
        // Hibernate flush tries to UPDATE deals WHERE id=? and gets 0 rows (StaleStateException).
        // Avoid mutating the managed entity and rely on a native update that matches substring(id,1,16)=uuid_to_bin(...).
        OffsetDateTime modifiedAt = OffsetDateTime.now();

        DealStageHistory hist = new DealStageHistory();
        hist.setDeal(deal);
        hist.setPreviousStage(previous);
        hist.setNewStage(newStage);
        hist.setChangedBy(userId);
        hist.setChangedAt(OffsetDateTime.now());
        hist.setNote(note);
        historyRepository.save(hist);

        int updated = dealRepository.updateStageCompat(dealId.toString(), newStage.name(), modifiedAt, userId);
        if (updated != 1) throw new IllegalStateException("Failed to update deal stage");
        return dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
    }

    public Page<DealStageHistory> getStageHistory(UUID dealId, Pageable pageable) {
        java.util.List<DealStageHistory> all = historyRepository.findByDealIdCompatOrderByChangedAtDesc(dealId.toString());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        java.util.List<DealStageHistory> slice = start >= end ? java.util.List.of() : all.subList(start, end);
        return new PageImpl<>(slice, pageable, all.size());
    }
}
