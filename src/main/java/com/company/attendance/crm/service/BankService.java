package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.repository.BankRepository;
import com.company.attendance.crm.repository.BankSpecifications;
import com.company.attendance.exception.ResourceNotFoundException;
import com.company.attendance.exception.InvalidForeignKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class BankService {
    private final BankRepository bankRepository;
    private final AuditService auditService;

    public BankService(BankRepository bankRepository, AuditService auditService) { 
        this.bankRepository = bankRepository; 
        this.auditService = auditService;
    }

    private String normalize(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public Bank create(Bank bank){
        if (bank.getName() == null || bank.getName().isBlank()){
            throw new IllegalArgumentException("name is required");
        }

        String normalizedName   = normalize(bank.getName());
        String normalizedBranch = normalize(bank.getBranchName());

        Optional<Bank> match = bankRepository.findAll().stream()
            .filter(b -> normalizedName.equals(normalize(b.getName())))
            .filter(b -> {
                if (normalizedBranch == null || normalizedBranch.isEmpty()) return true;
                return normalizedBranch.equals(normalize(b.getBranchName()));
            })
            .findFirst();

        if (match.isPresent()) {
            log.debug("Bank already exists: {} ({}), reusing id={}", normalizedName, normalizedBranch, match.get().getId());
            return match.get();
        }
        
        // Set owner from logged-in user
        if (bank.getCreatedBy() == null) {
            bank.setCreatedBy(auditService.getCurrentUserId() != null ? auditService.getCurrentUserId().longValue() : null);
        }
        bank.setActive(true); // always active on create
        auditService.setAuditFields(bank);
        return bankRepository.save(bank);
    }

    public Optional<Bank> get(Long id) {
        return bankRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Bank> list(Pageable pageable) {
        // active field maps to is_active TINYINT(1) — use equal(true) OR isNull
        Specification<Bank> spec = (root, query, cb) ->
            cb.or(
                cb.equal(root.get("active"), true),
                cb.isNull(root.get("active"))
            );
        return bankRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bank> search(Boolean active, Long ownerId, String q, Pageable pageable){
        Specification<Bank> spec;
        if (active == null || Boolean.TRUE.equals(active)) {
            // include active=true AND active=null (Excel-imported banks have null)
            spec = (root, query, cb) ->
                cb.or(cb.equal(root.get("active"), true), cb.isNull(root.get("active")));
        } else {
            spec = (root, query, cb) -> cb.equal(root.get("active"), false);
        }
        spec = spec.and(BankSpecifications.owner(ownerId)).and(BankSpecifications.q(q));
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
