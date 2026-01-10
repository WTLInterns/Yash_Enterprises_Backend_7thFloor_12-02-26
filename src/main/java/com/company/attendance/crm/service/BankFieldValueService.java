package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public List<BankFieldValue> list(UUID bankId){
        Bank bank = bankRepository.findById(bankId).orElseThrow(() -> new IllegalArgumentException("Bank not found"));
        return valRepo.findByBank(bank);
    }

    public BankFieldValue upsert(UUID bankId, String fieldKey, String value){
        Bank bank = bankRepository.findById(bankId).orElseThrow(() -> new IllegalArgumentException("Bank not found"));
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
