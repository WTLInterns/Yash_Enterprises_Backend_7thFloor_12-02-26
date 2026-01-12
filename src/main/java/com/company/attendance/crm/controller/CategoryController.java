package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ProductCategory;
import com.company.attendance.crm.repository.ProductCategoryRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final ProductCategoryRepository categoryRepository;
    
    @GetMapping
    public ResponseEntity<List<ProductCategory>> list() {
        log.info("GET /api/categories - Fetching all categories");
        try {
            List<ProductCategory> categories = categoryRepository.findByActiveTrueOrderByNameAsc();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Error fetching categories: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<ProductCategory> create(@Valid @RequestBody ProductCategory category) {
        log.info("POST /api/categories - Creating category: {}", category.getName());
        try {
            if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
                return ResponseEntity.badRequest().build();
            }
            
            ProductCategory saved = categoryRepository.save(category);
            return ResponseEntity.created(URI.create("/api/categories/" + saved.getId())).body(saved);
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
