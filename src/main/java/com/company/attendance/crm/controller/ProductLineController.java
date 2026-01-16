package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ProductLine;
import com.company.attendance.crm.service.ProductLineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/deals/{dealId}/product-lines")
public class ProductLineController {
    private final ProductLineService productLineService;

    public ProductLineController(ProductLineService productLineService) {
        this.productLineService = productLineService;
    }

    @GetMapping
    public List<ProductLine> list(@PathVariable Long dealId){
        return productLineService.list(dealId);
    }

    @PostMapping
    public ResponseEntity<ProductLine> create(@PathVariable Long dealId, @RequestBody ProductLine product,
                                              @RequestHeader(value = "X-User-Id", required = false) Integer userId){
        ProductLine created = productLineService.create(dealId, product, userId);
        return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/product-lines/"+created.getId())).body(created);
    }

    @PutMapping("/{productId}")
    public ProductLine update(@PathVariable Long dealId, @PathVariable Integer productId, @RequestBody ProductLine incoming){
        return productLineService.update(dealId, productId, incoming);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long dealId, @PathVariable Integer productId){
        productLineService.delete(dealId, productId);
        return ResponseEntity.noContent().build();
    }
}
