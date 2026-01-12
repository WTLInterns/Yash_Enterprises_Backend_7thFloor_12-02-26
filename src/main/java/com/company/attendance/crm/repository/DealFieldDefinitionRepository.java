package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.DealFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DealFieldDefinitionRepository extends JpaRepository<DealFieldDefinition, Integer> {
    boolean existsByFieldKeyIgnoreCase(String fieldKey);
    Optional<DealFieldDefinition> findByFieldKey(String fieldKey);
}
