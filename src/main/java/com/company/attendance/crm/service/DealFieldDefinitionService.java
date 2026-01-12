package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.DealFieldDefinition;
import com.company.attendance.crm.repository.DealFieldDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealFieldDefinitionService {
    private final DealFieldDefinitionRepository repo;

    public DealFieldDefinitionService(DealFieldDefinitionRepository repo) {
        this.repo = repo;
    }

    public DealFieldDefinition create(DealFieldDefinition def) {
        if (def.getFieldKey() == null || def.getFieldKey().isBlank()) throw new IllegalArgumentException("fieldKey is required");
        if (repo.existsByFieldKeyIgnoreCase(def.getFieldKey())) throw new IllegalArgumentException("fieldKey already exists");
        return repo.save(def);
    }

    public List<DealFieldDefinition> list() {
        return repo.findAll();
    }

    public DealFieldDefinition update(Integer id, DealFieldDefinition incoming) {
        DealFieldDefinition db = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Field definition not found"));
        db.setFieldName(incoming.getFieldName());
        db.setFieldType(incoming.getFieldType());
        db.setOptionsJson(incoming.getOptionsJson());
        db.setRequired(incoming.getRequired());
        db.setActive(incoming.getActive());
        return repo.save(db);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    public boolean validateValue(String fieldKey, String value) {
        DealFieldDefinition def = repo.findByFieldKey(fieldKey)
                .orElseThrow(() -> new IllegalArgumentException("Field definition not found for key: " + fieldKey));
        if (Boolean.TRUE.equals(def.getRequired()) && (value == null || value.isBlank())) return false;
        if (value == null || value.isBlank()) return true;
        try {
            return switch (def.getFieldType()) {
                case "NUMBER" -> { Double.parseDouble(value); yield true; }
                case "BOOLEAN" -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
                case "DATE" -> { java.time.LocalDate.parse(value); yield true; }
                case "TEXT", "DROPDOWN" -> true;
                default -> true;
            };
        } catch (Exception e) {
            return false;
        }
    }
}
