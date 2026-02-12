package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.BankFieldDefinition;
import com.company.attendance.crm.repository.BankFieldDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankFieldDefinitionService {
    private final BankFieldDefinitionRepository repo;

    public BankFieldDefinitionService(BankFieldDefinitionRepository repo) { this.repo = repo; }

    public BankFieldDefinition create(BankFieldDefinition d){
        if (repo.existsByFieldKeyIgnoreCase(d.getFieldKey())) throw new IllegalArgumentException("field key exists");
        return repo.save(d);
    }
    public List<BankFieldDefinition> list(){ return repo.findAll(); }
    public BankFieldDefinition update(Long id, BankFieldDefinition incoming){
        BankFieldDefinition db = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("not found"));
        db.setFieldName(incoming.getFieldName());
        db.setFieldType(incoming.getFieldType());
        db.setOptionsJson(incoming.getOptionsJson());
        db.setRequired(incoming.getRequired());
        db.setActive(incoming.getActive());
        return repo.save(db);
    }
    public void delete(Long id){ repo.deleteById(id); }

    public boolean validateValue(String fieldKey, String value){
        BankFieldDefinition def = repo.findByFieldKey(fieldKey).orElseThrow(() -> new IllegalArgumentException("Field not found"));
        if (Boolean.TRUE.equals(def.getRequired()) && (value == null || value.isBlank())) return false;
        if (value == null || value.isBlank()) return true;
        try {
            return switch (def.getFieldType()){
                case "NUMBER" -> { Double.parseDouble(value); yield true; }
                case "BOOLEAN" -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
                case "DATE" -> { java.time.LocalDate.parse(value); yield true; }
                case "PHONE" -> value.matches("[+0-9()\\- ]{6,20}");
                case "URL" -> value.matches("https?://.+");
                default -> true;
            };
        } catch (Exception e){ return false; }
    }
}
