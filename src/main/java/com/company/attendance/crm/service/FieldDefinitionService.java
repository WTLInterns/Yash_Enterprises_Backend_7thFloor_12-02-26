package com.company.attendance.crm.service;

import com.company.attendance.crm.dto.FieldDefinitionDto;
import com.company.attendance.crm.entity.FieldDefinition;
import com.company.attendance.crm.repository.FieldDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FieldDefinitionService {
    
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final AuditService auditService;
    
    public List<FieldDefinitionDto> getFieldsByEntity(String entity) {
        List<FieldDefinition> fields = fieldDefinitionRepository.findByEntityAndActiveTrueOrderByFieldNameAsc(entity);
        return fields.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public FieldDefinitionDto createField(String entity, FieldDefinitionDto dto) {
        // Check if field key already exists
        if (fieldDefinitionRepository.existsByEntityAndFieldKeyAndActiveTrue(entity, dto.getFieldKey())) {
            throw new IllegalArgumentException("Field with key '" + dto.getFieldKey() + "' already exists for entity: " + entity);
        }
        
        FieldDefinition field = toEntity(dto);
        field.setEntity(entity);
        field.setActive(true);
        
        // Set audit fields
        field.setCreatedBy(auditService.getCurrentUserId());
        field.setCreatedAt(Instant.now());
        
        FieldDefinition saved = fieldDefinitionRepository.save(field);
        return toDto(saved);
    }
    
    @Transactional
    public FieldDefinitionDto updateField(Long id, FieldDefinitionDto dto) {
        FieldDefinition existing = fieldDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Field definition not found"));
        
        existing.setFieldName(dto.getFieldName());
        existing.setFieldType(dto.getFieldType());
        existing.setRequired(dto.getRequired());
        existing.setOptionsJson(dto.getOptionsJson());
        
        // Set audit fields
        existing.setUpdatedBy(auditService.getCurrentUserId());
        existing.setUpdatedAt(Instant.now());
        
        FieldDefinition saved = fieldDefinitionRepository.save(existing);
        return toDto(saved);
    }
    
    @Transactional
    public void deleteField(Long id) {
        FieldDefinition field = fieldDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Field definition not found"));
        field.setActive(false);
        fieldDefinitionRepository.save(field);
    }
    
    private FieldDefinitionDto toDto(FieldDefinition entity) {
        FieldDefinitionDto dto = new FieldDefinitionDto();
        dto.setId(entity.getId());
        dto.setEntity(entity.getEntity());
        dto.setFieldKey(entity.getFieldKey());
        dto.setFieldName(entity.getFieldName());
        dto.setFieldType(entity.getFieldType());
        dto.setRequired(entity.getRequired());
        dto.setOptionsJson(entity.getOptionsJson());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }
    
    private FieldDefinition toEntity(FieldDefinitionDto dto) {
        FieldDefinition entity = new FieldDefinition();
        entity.setFieldKey(dto.getFieldKey());
        entity.setFieldName(dto.getFieldName());
        entity.setFieldType(dto.getFieldType());
        entity.setRequired(dto.getRequired());
        entity.setOptionsJson(dto.getOptionsJson());
        return entity;
    }
}
