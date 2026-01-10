package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ProductLine;
import com.company.attendance.crm.service.ProductLineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals/{dealId}/product-lines")
public class ProductLineController {
    private final ProductLineService productLineService;

    public ProductLineController(ProductLineService productLineService) {
        this.productLineService = productLineService;
    }

    @GetMapping
    public List<ProductLine> list(@PathVariable UUID dealId){
        return productLineService.list(dealId);
    }

    @PostMapping
    public ResponseEntity<ProductLine> create(@PathVariable UUID dealId, @RequestBody ProductLine product,
                                              @RequestHeader(value = "X-User-Id", required = false) UUID userId){
        ProductLine created = productLineService.create(dealId, product, userId);
        return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/product-lines/"+created.getId())).body(created);
    }

    @PutMapping("/{productId}")
    public ProductLine update(@PathVariable UUID dealId, @PathVariable UUID productId, @RequestBody ProductLine incoming){
        return productLineService.update(dealId, productId, incoming);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable UUID dealId, @PathVariable UUID productId){
        productLineService.delete(dealId, productId);
        return ResponseEntity.noContent().build();
    }
}
