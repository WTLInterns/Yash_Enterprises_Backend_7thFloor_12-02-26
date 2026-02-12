package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankFieldValueService {
    private final BankRepository bankRepository;
    private final BankFieldDefinitionRepository defRepo;
    private final BankFieldValueRepository valRepo;

    public BankFieldValueService(BankRepository bankRepository, BankFieldDefinitionRepository defRepo, BankFieldValueRepository valRepo) {
        this.bankRepository = bankRepository;
        this.defRepo = defRepo;
        this.valRepo = valRepo;
    }

    public List<BankFieldValue> list(Long bankId){
        Bank bank = bankRepository.findByIdSafe(bankId);
        return valRepo.findByBank(bank);
    }

    public BankFieldValue upsert(Long bankId, String fieldKey, String value){
        Bank bank = bankRepository.findByIdSafe(bankId);
        BankFieldDefinition def = defRepo.findByFieldKey(fieldKey).orElseThrow(() -> new IllegalArgumentException("Field not found"));
        BankFieldValue existing = valRepo.findByBankAndFieldDefinition(bank, def).orElse(null);
        if (existing == null){
            existing = new BankFieldValue();
            existing.setBank(bank);
            existing.setFieldDefinition(def);
        }
        existing.setValue(value);
        return valRepo.save(existing);
    }
}
