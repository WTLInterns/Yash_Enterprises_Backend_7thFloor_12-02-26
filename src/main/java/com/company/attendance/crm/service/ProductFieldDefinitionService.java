package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.ProductFieldDefinition;
import com.company.attendance.crm.repository.ProductFieldDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductFieldDefinitionService {
    private final ProductFieldDefinitionRepository defRepo;

    public ProductFieldDefinitionService(ProductFieldDefinitionRepository defRepo) {
        this.defRepo = defRepo;
    }

    public ProductFieldDefinition create(ProductFieldDefinition def){
        if (def.getFieldKey() == null || def.getFieldKey().isBlank()) throw new IllegalArgumentException("fieldKey is required");
        if (defRepo.existsByFieldKeyIgnoreCase(def.getFieldKey())) throw new IllegalArgumentException("fieldKey already exists");
        return defRepo.save(def);
    }

    public List<ProductFieldDefinition> list(){
        return defRepo.findAll();
    }

    public ProductFieldDefinition update(UUID id, ProductFieldDefinition incoming){
        ProductFieldDefinition db = defRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Field definition not found"));
        db.setFieldName(incoming.getFieldName());
        db.setFieldType(incoming.getFieldType());
        db.setRequired(incoming.getRequired());
        db.setActive(incoming.getActive());
        return defRepo.save(db);
    }

    public void delete(UUID id){
        defRepo.deleteById(id);
    }

    public boolean validateValue(String fieldKey, String value){
        ProductFieldDefinition def = defRepo.findByFieldKey(fieldKey)
                .orElseThrow(() -> new IllegalArgumentException("Field definition not found for key: "+fieldKey));
        if (Boolean.TRUE.equals(def.getRequired()) && (value == null || value.isBlank())) return false;
        String type = def.getFieldType();
        if (value == null || value.isBlank()) return true; // allow blank if not required
        try {
            switch (type) {
                case "NUMBER" -> { Double.parseDouble(value); return true; }
                case "BOOLEAN" -> { return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"); }
                case "DATE" -> { java.time.LocalDate.parse(value); return true; }
                case "TEXT", "DROPDOWN" -> { return true; }
                default -> { return true; }
            }
        } catch (Exception e) { return false; }
    }
}
