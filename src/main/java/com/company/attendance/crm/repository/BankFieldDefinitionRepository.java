package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.BankFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankFieldDefinitionRepository extends JpaRepository<BankFieldDefinition, Long> {
    boolean existsByFieldKeyIgnoreCase(String fieldKey);
    Optional<BankFieldDefinition> findByFieldKey(String fieldKey);
}
