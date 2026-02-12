package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.ProductCategory;
import com.company.attendance.crm.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {
    private final ProductCategoryRepository repo;

    public ProductCategoryService(ProductCategoryRepository repo) { this.repo = repo; }

    public ProductCategory create(ProductCategory c){
        if (c.getName() == null || c.getName().isBlank()) throw new IllegalArgumentException("name is required");
        if (repo.existsByNameIgnoreCase(c.getName())) throw new IllegalArgumentException("category exists");
        return repo.save(c);
    }

    public List<ProductCategory> list(){ return repo.findAll(); }

    public ProductCategory update(Long id, ProductCategory incoming){
        ProductCategory db = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("category not found"));
        db.setName(incoming.getName());
        if (incoming.getActive() != null) db.setActive(incoming.getActive());
        return repo.save(db);
    }

    public void delete(Long id){ repo.deleteById(id); }
}
