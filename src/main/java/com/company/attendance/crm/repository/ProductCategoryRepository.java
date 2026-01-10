package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsById(UUID id);
}
