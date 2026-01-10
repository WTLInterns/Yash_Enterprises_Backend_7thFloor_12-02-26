package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.ActivityFieldDefinition;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.ActivityFieldDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityFieldDefinitionService {
    private final ActivityFieldDefinitionRepository repo;

    public ActivityFieldDefinitionService(ActivityFieldDefinitionRepository repo) { this.repo = repo; }

    public ActivityFieldDefinition create(ActivityFieldDefinition d){
        if (repo.existsByFieldKeyIgnoreCaseAndActivityType(d.getFieldKey(), d.getActivityType()))
            throw new IllegalArgumentException("field key exists for type");
        return repo.save(d);
    }
    public List<ActivityFieldDefinition> list(ActivityType type){
        return type == null ? repo.findAll() : repo.findByActivityTypeAndActiveTrue(type);
    }
    public ActivityFieldDefinition update(UUID id, ActivityFieldDefinition incoming){
        ActivityFieldDefinition db = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("not found"));
        db.setFieldName(incoming.getFieldName());
        db.setFieldType(incoming.getFieldType());
        db.setRequired(incoming.getRequired());
        db.setActive(incoming.getActive());
        return repo.save(db);
    }
    public void delete(UUID id){ repo.deleteById(id); }

    public boolean validateValue(ActivityType type, String fieldKey, String value){
        ActivityFieldDefinition def = repo.findByFieldKeyAndActivityType(fieldKey, type)
                .orElseThrow(() -> new IllegalArgumentException("Field not found"));
        if (Boolean.TRUE.equals(def.getRequired()) && (value == null || value.isBlank())) return false;
        if (value == null || value.isBlank()) return true;
        try {
            return switch (def.getFieldType()){
                case "NUMBER" -> { Double.parseDouble(value); yield true; }
                case "BOOLEAN" -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
                case "DATE" -> { java.time.LocalDate.parse(value); yield true; }
                case "DATETIME" -> { java.time.OffsetDateTime.parse(value); yield true; }
                case "URL" -> value.matches("https?://.+");
                default -> true;
            };
        } catch (Exception e){ return false; }
    }
}
