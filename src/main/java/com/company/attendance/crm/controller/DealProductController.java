package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.service.DealProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Deal Products")
@RestController
@RequestMapping("/api/deals/{dealId}/products")
public class DealProductController {
    private final DealProductService service;

    public DealProductController(DealProductService service) { this.service = service; }

    @GetMapping
    public List<DealProduct> list(@PathVariable UUID dealId){
        return service.list(dealId);
    }

    @PostMapping
    public ResponseEntity<DealProduct> create(@PathVariable UUID dealId, @RequestBody DealProduct payload){
        UUID productId = payload.getProduct() != null ? payload.getProduct().getId() : null;
        DealProduct created = service.create(dealId, productId, payload);
        return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/products/"+created.getId())).body(created);
    }

    @PutMapping("/{dealProductId}")
    public DealProduct update(@PathVariable UUID dealId, @PathVariable UUID dealProductId, @RequestBody DealProduct payload){
        return service.update(dealId, dealProductId, payload);
    }

    @DeleteMapping("/{dealProductId}")
    public ResponseEntity<Void> delete(@PathVariable UUID dealId, @PathVariable UUID dealProductId){
        service.delete(dealId, dealProductId);
        return ResponseEntity.noContent().build();
    }
}
