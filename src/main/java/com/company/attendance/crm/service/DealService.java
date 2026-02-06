package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DealService {
    private final DealRepository dealRepository;
    private final AuditService auditService;

    public Optional<Deal> findById(Long id) {
        return dealRepository.findById(id);
    }

    public Page<Deal> list(Pageable pageable) {
        return dealRepository.findAll(pageable);
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    @Transactional
    public Deal create(Deal deal) {
        // Set audit fields
        auditService.setAuditFields(deal);
        return dealRepository.save(deal);
    }

    @Transactional
    public Deal update(Deal deal) {
        // Update audit fields
        auditService.updateAuditFields(deal);
        return dealRepository.save(deal);
    }

    public void delete(Long id) {
        dealRepository.deleteById(id);
    }
}
