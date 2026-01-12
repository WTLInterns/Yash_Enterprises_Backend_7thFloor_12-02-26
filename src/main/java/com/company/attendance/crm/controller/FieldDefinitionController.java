package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.FieldDefinitionDto;
import com.company.attendance.crm.service.FieldDefinitionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/fields")
@Tag(name = "Field Definitions")
@RequiredArgsConstructor
@Slf4j
public class FieldDefinitionController {
    
    private final FieldDefinitionService fieldDefinitionService;
    
    @GetMapping
    public ResponseEntity<List<FieldDefinitionDto>> getFieldsByEntity(@RequestParam String entity) {
        log.info("GET /api/fields?entity={} - Fetching field definitions", entity);
        try {
            List<FieldDefinitionDto> fields = fieldDefinitionService.getFieldsByEntity(entity);
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            log.error("Error fetching field definitions: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<FieldDefinitionDto> createField(
            @RequestParam String entity,
            @Valid @RequestBody FieldDefinitionDto fieldDefinitionDto) {
        log.info("POST /api/fields?entity={} - Creating field definition: {}", entity, fieldDefinitionDto.getFieldKey());
        try {
            FieldDefinitionDto created = fieldDefinitionService.createField(entity, fieldDefinitionDto);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.error("Error creating field definition: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating field definition: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FieldDefinitionDto> updateField(
            @PathVariable Long id,
            @Valid @RequestBody FieldDefinitionDto fieldDefinitionDto) {
        log.info("PUT /api/fields/{} - Updating field definition", id);
        try {
            FieldDefinitionDto updated = fieldDefinitionService.updateField(id, fieldDefinitionDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error updating field definition: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating field definition: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        log.info("DELETE /api/fields/{} - Deleting field definition", id);
        try {
            fieldDefinitionService.deleteField(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting field definition: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting field definition: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
