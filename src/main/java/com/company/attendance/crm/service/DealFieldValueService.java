package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealFieldDefinition;
import com.company.attendance.crm.entity.DealFieldValue;
import com.company.attendance.crm.repository.DealFieldDefinitionRepository;
import com.company.attendance.crm.repository.DealFieldValueRepository;
import com.company.attendance.crm.repository.DealRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealFieldValueService {
    private final DealRepository dealRepository;
    private final DealFieldDefinitionRepository defRepo;
    private final DealFieldValueRepository valRepo;

    public DealFieldValueService(DealRepository dealRepository, DealFieldDefinitionRepository defRepo, DealFieldValueRepository valRepo) {
        this.dealRepository = dealRepository;
        this.defRepo = defRepo;
        this.valRepo = valRepo;
    }

    public List<DealFieldValue> list(Long dealId) {
        Deal deal = dealRepository.findByIdSafe(dealId);
        return valRepo.findByDeal(deal);
    }

    public DealFieldValue upsert(Long dealId, String fieldKey, String value) {
        Deal deal = dealRepository.findByIdSafe(dealId);
        DealFieldDefinition def = defRepo.findByFieldKey(fieldKey).orElseThrow(() -> new IllegalArgumentException("Field not found"));
        DealFieldValue existing = valRepo.findByDealAndFieldDefinition(deal, def).orElse(null);
        if (existing == null) {
            existing = new DealFieldValue();
            existing.setDeal(deal);
            existing.setFieldDefinition(def);
        }
        existing.setValue(value);
        return valRepo.save(existing);
    }
}
