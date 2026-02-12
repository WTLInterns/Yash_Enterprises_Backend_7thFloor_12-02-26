package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<ProductCategory> findByActiveTrueOrderByNameAsc();
}
