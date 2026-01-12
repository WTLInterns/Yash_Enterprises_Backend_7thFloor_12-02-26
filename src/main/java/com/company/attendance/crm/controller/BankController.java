package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.mapper.SimpleCrmMapper;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.service.AuditService;
import com.company.attendance.crm.service.BankService;
import com.company.attendance.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Banks")
@RestController
@RequestMapping("/api/banks")
public class BankController {
    private final BankService bankService;
    private final DealRepository dealRepository;
    private final SimpleCrmMapper simpleCrmMapper;
    private final AuditService auditService;

    public BankController(BankService bankService, DealRepository dealRepository, SimpleCrmMapper simpleCrmMapper, AuditService auditService) {
        this.bankService = bankService;
        this.dealRepository = dealRepository;
        this.simpleCrmMapper = simpleCrmMapper;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<BankDto> create(@RequestBody BankDto bankDto){
        Bank bank = simpleCrmMapper.toBankEntity(bankDto);
        Bank created = bankService.create(bank);
        BankDto response = simpleCrmMapper.toBankDto(created);
        // Owner is whoever just created the bank
        Integer ownerId = created.getUpdatedBy() != null ? created.getUpdatedBy() : created.getCreatedBy();
        response.setOwnerName(auditService.getUserName(ownerId));
        response.setCreatedByName(auditService.getUserName(created.getCreatedBy()));
        response.setUpdatedByName(auditService.getUserName(created.getUpdatedBy()));
        return ResponseEntity.created(URI.create("/api/banks/"+created.getId())).body(response);
    }

    @GetMapping
    public Page<BankDto> list(@RequestParam(value = "active", required = false) Boolean active,
                           @RequestParam(value = "ownerId", required = false) Integer ownerId,
                           @RequestParam(value = "q", required = false) String q,
                           Pageable pageable){
        // Default to active=true unless explicitly requested otherwise
        if (active == null && ownerId == null && (q == null || q.isBlank())) {
            Page<Bank> banks = bankService.list(pageable); // This returns only active=true
            List<BankDto> dtos = banks.getContent().stream()
                .map(bank -> {
                    BankDto dto = simpleCrmMapper.toBankDto(bank);
                    Integer owner = bank.getUpdatedBy() != null ? bank.getUpdatedBy() : bank.getCreatedBy();
                    dto.setOwnerName(auditService.getUserName(owner));
                    dto.setCreatedByName(auditService.getUserName(bank.getCreatedBy()));
                    dto.setUpdatedByName(auditService.getUserName(bank.getUpdatedBy()));
                    return dto;
                })
                .collect(Collectors.toList());
            return new PageImpl<>(dtos, pageable, banks.getTotalElements());
        }
        Boolean effectiveActive = (active == null ? Boolean.TRUE : active);
        Page<Bank> banks = bankService.search(effectiveActive, ownerId, q, pageable);
        List<BankDto> dtos = banks.getContent().stream()
            .map(bank -> {
                BankDto dto = simpleCrmMapper.toBankDto(bank);
                Integer owner = bank.getUpdatedBy() != null ? bank.getUpdatedBy() : bank.getCreatedBy();
                dto.setOwnerName(auditService.getUserName(owner));
                dto.setCreatedByName(auditService.getUserName(bank.getCreatedBy()));
                dto.setUpdatedByName(auditService.getUserName(bank.getUpdatedBy()));
                return dto;
            })
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, banks.getTotalElements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankDto> getById(@PathVariable Integer id) {
        Bank bank = bankService.get(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bank not found"));
        BankDto bankDto = simpleCrmMapper.toBankDto(bank);
        Integer owner = bank.getUpdatedBy() != null ? bank.getUpdatedBy() : bank.getCreatedBy();
        bankDto.setOwnerName(auditService.getUserName(owner));
        bankDto.setCreatedByName(auditService.getUserName(bank.getCreatedBy()));
        bankDto.setUpdatedByName(auditService.getUserName(bank.getUpdatedBy()));
        return ResponseEntity.ok(bankDto);
    }

    @PutMapping("/{id}")
    public BankDto update(@PathVariable Integer id, @RequestBody BankDto bankDto){
        Bank updated = bankService.update(id, bankDto);
        BankDto dto = simpleCrmMapper.toBankDto(updated);
        Integer owner = updated.getUpdatedBy() != null ? updated.getUpdatedBy() : updated.getCreatedBy();
        dto.setOwnerName(auditService.getUserName(owner));
        dto.setCreatedByName(auditService.getUserName(updated.getCreatedBy()));
        dto.setUpdatedByName(auditService.getUserName(updated.getUpdatedBy()));
        return dto;
    }

    @PatchMapping("/{id}/status")
    public Bank patchStatus(@PathVariable Integer id, @RequestParam("active") boolean active){
        return bankService.patchStatus(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        bankService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // lookup for dropdowns
    @GetMapping("/lookup")
    public List<Map<String, Object>> lookup(@RequestParam(value = "q", required = false) String q, Pageable pageable){
        Page<Bank> page = bankService.search(true, null, q, pageable);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Bank b : page.getContent()){
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("name", b.getName());
            items.add(m);
        }
        return items;
    }

    // usage
    @GetMapping("/{bankId}/deals")
    public List<Map<String, Object>> usage(@PathVariable Long bankId){
        List<Deal> deals = dealRepository.findByClientId(bankId);
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Deal d : deals){
            Map<String, Object> row = new HashMap<>();
            row.put("dealId", d.getId());
            row.put("dealName", d.getName());
            row.put("amount", d.getValueAmount());
            row.put("stage", d.getStage() != null ? d.getStage().name() : null);
            resp.add(row);
        }
        return resp;
    }

    // permissions helper
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Boolean>> permissions(@PathVariable Integer id,
                                                            @RequestHeader(value = "X-User-Role", required = false) String role,
                                                            @RequestHeader(value = "X-User-Id", required = false) Integer userId){
        return bankService.get(id).map(b -> {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = userId != null && userId.equals(b.getCreatedBy());
            Map<String, Boolean> map = new HashMap<>();
            map.put("canEdit", isAdmin || isOwner);
            map.put("canDelete", isAdmin);
            map.put("canDisable", isAdmin);
            return ResponseEntity.ok(map);
        }).orElse(ResponseEntity.notFound().build());
    }
}
