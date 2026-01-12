package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.FieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldDefinitionRepository extends JpaRepository<FieldDefinition, Long> {
    List<FieldDefinition> findByEntityAndActiveTrueOrderByFieldNameAsc(String entity);
    Optional<FieldDefinition> findByEntityAndFieldKeyAndActiveTrue(String entity, String fieldKey);
    boolean existsByEntityAndFieldKeyAndActiveTrue(String entity, String fieldKey);
}
