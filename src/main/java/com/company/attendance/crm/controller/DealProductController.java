package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.DealProductDto;
import com.company.attendance.crm.dto.DealProductRequestDto;
import com.company.attendance.crm.service.DealProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Deal Products")
@RestController
@RequestMapping("/api/deals/{dealId}/products")
public class DealProductController {
    private final DealProductService service;

    public DealProductController(DealProductService service) { this.service = service; }

    @GetMapping
    public List<DealProductDto> list(@PathVariable Long dealId){
        return service.list(dealId);
    }

    @PostMapping
    public ResponseEntity<DealProductDto> create(@PathVariable Long dealId, @RequestBody DealProductRequestDto payload){
        DealProductDto created = service.create(dealId, payload);
        return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/products/"+created.getId())).body(created);
    }

    @PutMapping("/{dealProductId}")
    public DealProductDto update(@PathVariable Long dealId, @PathVariable Long dealProductId, @RequestBody DealProductRequestDto payload){
        return service.update(dealId, dealProductId, payload);
    }

    @DeleteMapping("/{dealProductId}")
    public ResponseEntity<Void> delete(@PathVariable Long dealId, @PathVariable Long dealProductId){
        service.delete(dealId, dealProductId);
        return ResponseEntity.noContent().build();
    }
}
