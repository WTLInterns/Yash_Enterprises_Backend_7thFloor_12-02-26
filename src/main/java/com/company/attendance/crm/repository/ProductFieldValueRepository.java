package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ProductFieldValue;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.entity.ProductFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductFieldValueRepository extends JpaRepository<ProductFieldValue, Integer> {
    List<ProductFieldValue> findByProduct(Product product);
    Optional<ProductFieldValue> findByProductAndFieldDefinition(Product product, ProductFieldDefinition def);
}
