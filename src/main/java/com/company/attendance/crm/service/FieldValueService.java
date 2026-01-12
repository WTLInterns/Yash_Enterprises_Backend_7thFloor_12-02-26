package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.FieldValue;
import com.company.attendance.crm.repository.FieldValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FieldValueService {
    
    private final FieldValueRepository fieldValueRepository;
    
    public List<FieldValue> getValuesByEntityAndId(String entity, Long entityId) {
        return fieldValueRepository.findByEntityAndEntityIdOrderByFieldKeyAsc(entity, entityId);
    }
    
    @Transactional
    public void saveValues(String entity, Long entityId, Map<String, String> values) {
        // Delete existing values
        fieldValueRepository.deleteByEntityAndEntityId(entity, entityId);
        
        // Save new values
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                FieldValue fieldValue = new FieldValue();
                fieldValue.setEntity(entity);
                fieldValue.setEntityId(entityId);
                fieldValue.setFieldKey(entry.getKey());
                fieldValue.setValue(entry.getValue());
                fieldValueRepository.save(fieldValue);
            }
        }
    }
    
    @Transactional
    public FieldValue saveValue(String entity, Long entityId, String fieldKey, String value) {
        Optional<FieldValue> existing = fieldValueRepository.findByEntityAndEntityIdAndFieldKey(entity, entityId, fieldKey);
        
        if (existing.isPresent()) {
            FieldValue fieldValue = existing.get();
            fieldValue.setValue(value);
            return fieldValueRepository.save(fieldValue);
        } else {
            FieldValue fieldValue = new FieldValue();
            fieldValue.setEntity(entity);
            fieldValue.setEntityId(entityId);
            fieldValue.setFieldKey(fieldKey);
            fieldValue.setValue(value);
            return fieldValueRepository.save(fieldValue);
        }
    }
    
    @Transactional
    public void deleteValues(String entity, Long entityId) {
        fieldValueRepository.deleteByEntityAndEntityId(entity, entityId);
    }
}
