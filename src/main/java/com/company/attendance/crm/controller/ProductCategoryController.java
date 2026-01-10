package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ProductCategory;
import com.company.attendance.crm.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Product Categories")
@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {
    private final ProductCategoryService service;

    public ProductCategoryController(ProductCategoryService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ProductCategory> create(@RequestBody ProductCategory c){
        ProductCategory created = service.create(c);
        return ResponseEntity.created(URI.create("/api/product-categories/"+created.getId())).body(created);
    }

    @GetMapping
    public List<ProductCategory> list(){ return service.list(); }

    @PutMapping("/{id}")
    public ProductCategory update(@PathVariable UUID id, @RequestBody ProductCategory incoming){
        return service.update(id, incoming);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
