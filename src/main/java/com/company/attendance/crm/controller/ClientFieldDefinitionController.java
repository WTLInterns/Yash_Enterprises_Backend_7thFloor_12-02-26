package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ClientFieldDefinition;
import com.company.attendance.crm.repository.ClientFieldDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/client-fields")
@RequiredArgsConstructor
@Slf4j
public class ClientFieldDefinitionController {
    
    private final ClientFieldDefinitionRepository repository;
    
    @GetMapping
    public ResponseEntity<List<ClientFieldDefinition>> getFieldDefinitions() {
        log.info("GET /api/client-fields - Fetching client field definitions");
        List<ClientFieldDefinition> definitions = repository.findByActiveTrueOrderByOrderIndexAsc();
        return ResponseEntity.ok(definitions);
    }
    
    @PostMapping
    public ResponseEntity<ClientFieldDefinition> createFieldDefinition(@Valid @RequestBody ClientFieldDefinition definition) {
        log.info("POST /api/client-fields - Creating field definition: {}", definition.getFieldKey());
        
        if (repository.existsByFieldKey(definition.getFieldKey())) {
            return ResponseEntity.badRequest().build();
        }
        
        ClientFieldDefinition saved = repository.save(definition);
        return ResponseEntity.status(201).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClientFieldDefinition> updateFieldDefinition(
            @PathVariable Long id,
            @Valid @RequestBody ClientFieldDefinition definition) {
        log.info("PUT /api/client-fields/{} - Updating field definition", id);
        
        return repository.findById(id)
                .map(existing -> {
                    existing.setFieldName(definition.getFieldName());
                    existing.setFieldType(definition.getFieldType());
                    existing.setOptionsJson(definition.getOptionsJson());
                    existing.setRequired(definition.getRequired());
                    existing.setActive(definition.getActive());
                    existing.setOrderIndex(definition.getOrderIndex());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFieldDefinition(@PathVariable Long id) {
        log.info("DELETE /api/client-fields/{} - Deleting field definition", id);
        
        return repository.findById(id)
                .map(existing -> {
                    existing.setActive(false);
                    repository.save(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
