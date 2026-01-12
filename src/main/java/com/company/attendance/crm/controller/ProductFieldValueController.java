package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ProductFieldValue;
import com.company.attendance.crm.service.ProductFieldValueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Tag(name = "Product Field Values")
@RestController
@RequestMapping("/api/products/{productId}/fields")
public class ProductFieldValueController {
    private final ProductFieldValueService service;

    public ProductFieldValueController(ProductFieldValueService service) { this.service = service; }

    @GetMapping
    public List<ProductFieldValue> list(@PathVariable Long productId){
        return service.list(productId);
    }

    @PostMapping
    public ResponseEntity<ProductFieldValue> upsert(@PathVariable Long productId,
                                                    @RequestBody Map<String, String> payload){
        String fieldKey = payload.get("fieldKey");
        String value = payload.get("value");
        ProductFieldValue saved = service.upsert(productId, fieldKey, value);
        return ResponseEntity.created(URI.create("/api/products/"+productId+"/fields/"+saved.getId())).body(saved);
    }
}
