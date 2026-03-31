package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.repository.BankRepository;
import com.company.attendance.crm.repository.BankSpecifications;
import com.company.attendance.exception.ResourceNotFoundException;
import com.company.attendance.exception.InvalidForeignKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankService {
    private final BankRepository bankRepository;
    private final AuditService auditService;

    public BankService(BankRepository bankRepository, AuditService auditService) { 
        this.bankRepository = bankRepository; 
        this.auditService = auditService;
    }

    public Bank create(Bank bank){
        if (bank.getName() == null || bank.getName().isBlank()){
            throw new IllegalArgumentException("name is required");
        }
        
        // Check bank uniqueness by name + branch + taluka
        List<Bank> existing = bankRepository
            .findByNameIgnoreCaseAndBranchNameIgnoreCaseAndTalukaIgnoreCase(
                bank.getName(),
                bank.getBranchName(),
                bank.getTaluka()
            );
        
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        
        // Set owner from logged-in user
        if (bank.getCreatedBy() == null) {
            bank.setCreatedBy(auditService.getCurrentUserId() != null ? auditService.getCurrentUserId().longValue() : null);
        }
        
        // Set audit fields (createdBy, createdAt)
        auditService.setAuditFields(bank);
        return bankRepository.save(bank);
    }

    public Optional<Bank> get(Long id) {
        return bankRepository.findById(id);
    }

    public Page<Bank> list(Pageable pageable) {
        // Only return active banks (soft-delete: active=false means deleted)
        Specification<Bank> spec = BankSpecifications.active(true);
        return bankRepository.findAll(spec, pageable);
    }

    public Page<Bank> search(Boolean active, Long ownerId, String q, Pageable pageable){
        Specification<Bank> spec = Specification.where(BankSpecifications.active(active))
                .and(BankSpecifications.owner(ownerId))
                .and(BankSpecifications.q(q));
        return bankRepository.findAll(spec, pageable);
    }

    public Bank update(Long id, BankDto dto) {
        // Find existing bank
        Bank existing = bankRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bank not found"));
        
        // Update fields
        if (dto.getName() != null && !dto.getName().isBlank()){
            existing.setName(dto.getName());
        }
        if (dto.getAddress() != null){
            existing.setAddress(dto.getAddress());
        }
        if (dto.getPhone() != null){
            existing.setPhone(dto.getPhone());
        }
        if (dto.getWebsite() != null){
            existing.setWebsite(dto.getWebsite());
        }
        if (dto.getDescription() != null){
            existing.setDescription(dto.getDescription());
        }
        if (dto.getDistrict() != null){
            existing.setDistrict(dto.getDistrict());
        }
        if (dto.getTaluka() != null){
            existing.setTaluka(dto.getTaluka());
        }
        if (dto.getActive() != null){
            existing.setActive(dto.getActive());
        }
        
        // Update owner if not set
        if (existing.getCreatedBy() == null) {
            existing.setCreatedBy(auditService.getCurrentUserId() != null ? auditService.getCurrentUserId().longValue() : null);
        }
        
        // Update audit fields (updatedBy, updatedAt)
        auditService.updateAuditFields(existing);
        
        return bankRepository.save(existing);
    }

    public Bank patchStatus(Long id, boolean active){
        Bank db = bankRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Bank not found"));
        db.setActive(active);
        return bankRepository.save(db);
    }

    public void delete(Long id){
        Bank b = bankRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bank not found"));
        b.setActive(false);
        bankRepository.save(b);
    }
}
