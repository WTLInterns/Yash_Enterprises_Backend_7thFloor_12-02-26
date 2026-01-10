package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ProductFieldDefinition;
import com.company.attendance.crm.service.ProductFieldDefinitionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Product Field Definitions")
@RestController
@RequestMapping("/api/product-fields")
public class ProductFieldDefinitionController {
    private final ProductFieldDefinitionService service;

    public ProductFieldDefinitionController(ProductFieldDefinitionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ProductFieldDefinition> create(@RequestBody ProductFieldDefinition def){
        ProductFieldDefinition created = service.create(def);
        return ResponseEntity.created(URI.create("/api/product-fields/"+created.getId())).body(created);
    }

    @GetMapping
    public List<ProductFieldDefinition> list(){
        return service.list();
    }

    @PutMapping("/{id}")
    public ProductFieldDefinition update(@PathVariable UUID id, @RequestBody ProductFieldDefinition incoming){
        return service.update(id, incoming);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<java.util.Map<String, Object>> validate(@RequestParam("fieldKey") String fieldKey,
                                                                  @RequestParam(value = "value", required = false) String value){
        boolean valid = service.validateValue(fieldKey, value);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("fieldKey", fieldKey);
        resp.put("valid", valid);
        return ResponseEntity.ok(resp);
    }
}
