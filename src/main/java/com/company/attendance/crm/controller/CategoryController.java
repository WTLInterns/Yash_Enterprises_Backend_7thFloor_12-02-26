package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ProductCategory;
import com.company.attendance.crm.repository.ProductCategoryRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories")
public class CategoryController {
    
    private final ProductCategoryRepository categoryRepository;
    
    public CategoryController(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @GetMapping
    public List<ProductCategory> list() {
        List<ProductCategory> categories = categoryRepository.findAll();
        return categories.stream().map(cat -> {
            ProductCategory dto = new ProductCategory();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setActive(cat.getActive());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    @PostMapping
    public ResponseEntity<ProductCategory> create(@RequestBody ProductCategory category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            return ResponseEntity.badRequest().build();
        }
        
        ProductCategory saved = categoryRepository.save(category);
        return ResponseEntity.created(URI.create("/api/categories/" + saved.getId())).body(saved);
    }
}
