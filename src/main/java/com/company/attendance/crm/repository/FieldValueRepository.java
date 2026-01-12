package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.FieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldValueRepository extends JpaRepository<FieldValue, Long> {
    List<FieldValue> findByEntityAndEntityIdOrderByFieldKeyAsc(String entity, Long entityId);
    Optional<FieldValue> findByEntityAndEntityIdAndFieldKey(String entity, Long entityId, String fieldKey);
    void deleteByEntityAndEntityId(String entity, Long entityId);
}
