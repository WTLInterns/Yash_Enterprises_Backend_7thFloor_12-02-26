package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.FieldValue;
import com.company.attendance.crm.service.FieldValueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/field-values")
@Tag(name = "Field Values")
@RequiredArgsConstructor
@Slf4j
public class FieldValueController {
    
    private final FieldValueService fieldValueService;
    
    @GetMapping
    public ResponseEntity<List<FieldValue>> getValuesByEntityAndId(
            @RequestParam String entity,
            @RequestParam Long entityId) {
        log.info("GET /api/field-values?entity={}&entityId={} - Fetching field values", entity, entityId);
        try {
            List<FieldValue> values = fieldValueService.getValuesByEntityAndId(entity, entityId);
            return ResponseEntity.ok(values);
        } catch (Exception e) {
            log.error("Error fetching field values: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<FieldValue> saveValue(
            @RequestParam String entity,
            @RequestParam Long entityId,
            @RequestParam String fieldKey,
            @RequestBody String value) {
        log.info("POST /api/field-values?entity={}&entityId={}&fieldKey={} - Saving field value", entity, entityId, fieldKey);
        try {
            FieldValue saved = fieldValueService.saveValue(entity, entityId, fieldKey, value);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving field value: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/batch")
    public ResponseEntity<Void> saveValues(
            @RequestParam String entity,
            @RequestParam Long entityId,
            @RequestBody Map<String, String> values) {
        log.info("POST /api/field-values/batch?entity={}&entityId={} - Saving batch field values", entity, entityId);
        try {
            fieldValueService.saveValues(entity, entityId, values);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error saving batch field values: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping
    public ResponseEntity<Void> deleteValues(
            @RequestParam String entity,
            @RequestParam Long entityId) {
        log.info("DELETE /api/field-values?entity={}&entityId={} - Deleting field values", entity, entityId);
        try {
            fieldValueService.deleteValues(entity, entityId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting field values: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
