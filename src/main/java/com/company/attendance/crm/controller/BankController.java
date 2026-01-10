package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.service.BankService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@Tag(name = "Banks")
@RestController
@RequestMapping("/api/banks")
public class BankController {
    private final BankService bankService;
    private final DealRepository dealRepository;

    public BankController(BankService bankService, DealRepository dealRepository) {
        this.bankService = bankService;
        this.dealRepository = dealRepository;
    }

    @PostMapping
    public ResponseEntity<Bank> create(@RequestBody Bank bank){
        Bank created = bankService.create(bank);
        return ResponseEntity.created(URI.create("/api/banks/"+created.getId())).body(created);
    }

    @GetMapping
    public Page<Bank> list(@RequestParam(value = "active", required = false) Boolean active,
                           @RequestParam(value = "ownerId", required = false) UUID ownerId,
                           @RequestParam(value = "q", required = false) String q,
                           Pageable pageable){
        if (active == null && ownerId == null && (q == null || q.isBlank())) return bankService.list(pageable);
        return bankService.search(active, ownerId, q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bank> get(@PathVariable UUID id){
        return bankService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Bank update(@PathVariable UUID id, @RequestBody Bank incoming){
        return bankService.update(id, incoming);
    }

    @PatchMapping("/{id}/status")
    public Bank patchStatus(@PathVariable UUID id, @RequestParam("active") boolean active){
        return bankService.patchStatus(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
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
            m.put("bankName", b.getBankName());
            items.add(m);
        }
        return items;
    }

    // usage
    @GetMapping("/{bankId}/deals")
    public List<Map<String, Object>> usage(@PathVariable UUID bankId){
        List<Deal> deals = dealRepository.findByBankId(bankId);
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
    public ResponseEntity<Map<String, Boolean>> permissions(@PathVariable UUID id,
                                                            @RequestHeader(value = "X-User-Role", required = false) String role,
                                                            @RequestHeader(value = "X-User-Id", required = false) UUID userId){
        return bankService.get(id).map(b -> {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = userId != null && userId.equals(b.getOwnerId());
            Map<String, Boolean> map = new HashMap<>();
            map.put("canEdit", isAdmin || isOwner);
            map.put("canDelete", isAdmin);
            map.put("canDisable", isAdmin);
            return ResponseEntity.ok(map);
        }).orElse(ResponseEntity.notFound().build());
    }
}
