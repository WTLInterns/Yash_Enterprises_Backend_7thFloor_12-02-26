package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    boolean existsByProductNameIgnoreCase(String productName);
    Optional<Product> findByProductNameIgnoreCase(String productName);
}
