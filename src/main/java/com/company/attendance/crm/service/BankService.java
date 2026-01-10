package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.repository.BankRepository;
import com.company.attendance.crm.repository.BankSpecifications;
import com.company.attendance.exception.ResourceNotFoundException;
import com.company.attendance.exception.InvalidForeignKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BankService {
    private final BankRepository bankRepository;

    public BankService(BankRepository bankRepository) { 
        this.bankRepository = bankRepository; 
    }

    public Bank create(Bank bank){
        if (bank.getBankName() == null || bank.getBankName().isBlank()){
            throw new IllegalArgumentException("bankName is required");
        }
        if (bankRepository.existsByBankNameIgnoreCase(bank.getBankName())){
            throw new IllegalArgumentException("Bank name already exists");
        }
        return bankRepository.save(bank);
    }

    public Optional<Bank> get(UUID id){ return bankRepository.findById(id); }

    public Page<Bank> list(Pageable pageable){ return bankRepository.findAll(pageable); }

    public Page<Bank> search(Boolean active, UUID ownerId, String q, Pageable pageable){
        Specification<Bank> spec = Specification.where(BankSpecifications.active(active))
                .and(BankSpecifications.owner(ownerId))
                .and(BankSpecifications.q(q));
        return bankRepository.findAll(spec, pageable);
    }

    public Bank update(UUID id, Bank incoming){
        // Find existing bank
        Bank existing = bankRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bank not found"));
        
        // Preserve existing ownerId unless an explicit value is provided
        // Note: ownerId is supplied/derived as UUID; do not validate against Employee (Long id)
        
        // Check for duplicate bank name (excluding current bank)
        if (!existing.getBankName().equals(incoming.getBankName()) && 
            bankRepository.existsByBankNameIgnoreCase(incoming.getBankName())) {
            throw new IllegalArgumentException("Bank name already exists");
        }
        
        // Update only editable fields, keep system fields intact
        existing.setBankName(incoming.getBankName());
        existing.setBranchName(incoming.getBranchName());
        existing.setPhone(incoming.getPhone());
        existing.setWebsite(incoming.getWebsite());
        existing.setDescription(incoming.getDescription());
        existing.setAddress(incoming.getAddress());
        existing.setTaluka(incoming.getTaluka());
        existing.setDistrict(incoming.getDistrict());
        existing.setPinCode(incoming.getPinCode());
        if (incoming.getOwnerId() != null) {
            existing.setOwnerId(incoming.getOwnerId());
        }
        
        // Keep createdAt intact, update updatedAt
        // existing.setCreatedAt() - Don't touch this
        // existing.setUpdatedAt() - This will be handled by @PreUpdate
        
        if (incoming.getActive() != null) existing.setActive(incoming.getActive());
        
        return bankRepository.save(existing);
    }

    public Bank patchStatus(UUID id, boolean active){
        Bank db = bankRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Bank not found"));
        db.setActive(active);
        return bankRepository.save(db);
    }

    public void delete(UUID id){ bankRepository.deleteById(id); }
}
