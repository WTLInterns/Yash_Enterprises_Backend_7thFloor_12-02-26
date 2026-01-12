package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ClientFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientFieldDefinitionRepository extends JpaRepository<ClientFieldDefinition, Long> {
    
    List<ClientFieldDefinition> findByActiveTrueOrderByOrderIndexAsc();
    
    Optional<ClientFieldDefinition> findByFieldKeyAndActiveTrue(String fieldKey);
    
    boolean existsByFieldKey(String fieldKey);
}
