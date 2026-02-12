package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ProductFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductFieldDefinitionRepository extends JpaRepository<ProductFieldDefinition, Long> {
    Optional<ProductFieldDefinition> findByFieldKey(String fieldKey);
    boolean existsByFieldKeyIgnoreCase(String fieldKey);
}
