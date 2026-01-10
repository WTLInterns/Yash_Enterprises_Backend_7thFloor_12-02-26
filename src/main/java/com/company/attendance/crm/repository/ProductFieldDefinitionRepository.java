package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ProductFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductFieldDefinitionRepository extends JpaRepository<ProductFieldDefinition, UUID> {
    Optional<ProductFieldDefinition> findByFieldKey(String fieldKey);
    boolean existsByFieldKeyIgnoreCase(String fieldKey);
}
