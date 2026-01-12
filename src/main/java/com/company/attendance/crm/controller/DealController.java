package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.DealDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.service.DealService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/deals")
public class DealController {
    private final DealService dealService;
    private final DealRepository dealRepository;

    public DealController(DealService dealService, DealRepository dealRepository) {
        this.dealService = dealService;
        this.dealRepository = dealRepository;
    }

    @GetMapping
    public Page<Deal> list(Pageable pageable) {
        return dealService.list(pageable);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Deal>> getAllDeals() {
        return ResponseEntity.ok(dealService.getAllDeals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deal> getById(@PathVariable Integer id) {
        Optional<Deal> deal = dealRepository.findById(id);
        return deal.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Deal>> getByClientId(@PathVariable Long clientId) {
        List<Deal> deals = dealRepository.findByClientId(clientId);
        return ResponseEntity.ok(deals);
    }

    @PostMapping
    public ResponseEntity<Deal> create(@RequestBody DealDto dealDto) {
        Deal deal = new Deal();
        deal.setClientId(dealDto.getClientId() != null ? dealDto.getClientId().longValue() : null);
        deal.setName(dealDto.getName());
        deal.setValueAmount(dealDto.getValueAmount());
        deal.setClosingDate(dealDto.getClosingDate());
        deal.setBranchName(dealDto.getBranchName());
        deal.setRelatedBankName(dealDto.getBankName());
        deal.setDescription(dealDto.getDescription());
        deal.setRequiredAmount(dealDto.getRequiredAmount());
        deal.setOutstandingAmount(dealDto.getOutstandingAmount());
        deal.setStage(dealDto.getStage() != null ? com.company.attendance.crm.enums.DealStage.valueOf(dealDto.getStage()) : null);
        deal.setActive(dealDto.getActive());
        
        Deal created = dealService.create(deal);
        return ResponseEntity.created(URI.create("/api/deals/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deal> update(@PathVariable Integer id, @RequestBody DealDto dealDto) {
        Optional<Deal> existing = dealRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Deal deal = existing.get();
        deal.setClientId(dealDto.getClientId() != null ? dealDto.getClientId().longValue() : null);
        deal.setName(dealDto.getName());
        deal.setValueAmount(dealDto.getValueAmount());
        deal.setClosingDate(dealDto.getClosingDate());
        deal.setBranchName(dealDto.getBranchName());
        deal.setRelatedBankName(dealDto.getBankName());
        deal.setDescription(dealDto.getDescription());
        deal.setRequiredAmount(dealDto.getRequiredAmount());
        deal.setOutstandingAmount(dealDto.getOutstandingAmount());
        deal.setStage(dealDto.getStage() != null ? com.company.attendance.crm.enums.DealStage.valueOf(dealDto.getStage()) : null);
        deal.setActive(dealDto.getActive());
        
        Deal updated = dealService.update(deal);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        dealService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
