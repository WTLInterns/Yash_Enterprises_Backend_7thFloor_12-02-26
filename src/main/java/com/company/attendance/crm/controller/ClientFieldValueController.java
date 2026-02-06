package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ClientFieldValue;
import com.company.attendance.crm.service.ClientFieldValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients/{clientId}/fields")
@RequiredArgsConstructor
@Slf4j
public class ClientFieldValueController {

    private final ClientFieldValueService fieldValueService;

    @GetMapping
    public ResponseEntity<List<ClientFieldValue>> getClientFieldValues(@PathVariable Long clientId) {
        log.info("GET /api/clients/{}/fields - Fetching field values", clientId);
        List<ClientFieldValue> fieldValues = fieldValueService.getClientFieldValues(clientId);
        return ResponseEntity.ok(fieldValues);
    }

    @GetMapping("/map")
    public ResponseEntity<Map<String, String>> getClientFieldValuesAsMap(@PathVariable Long clientId) {
        log.info("GET /api/clients/{}/fields/map - Fetching field values as map", clientId);
        Map<String, String> fieldValues = fieldValueService.getClientFieldValuesAsMap(clientId);
        return ResponseEntity.ok(fieldValues);
    }

    @PostMapping
    public ResponseEntity<ClientFieldValue> upsertFieldValue(
            @PathVariable Long clientId,
            @RequestBody Map<String, String> payload) {
        
        String fieldKey = payload.get("fieldKey");
        String value = payload.get("value");
        
        log.info("POST /api/clients/{}/fields - Upserting field: {} with value: {}", clientId, fieldKey, value);
        
        if (fieldKey == null || fieldKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ClientFieldValue saved = fieldValueService.upsertFieldValue(clientId, fieldKey, value);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, String>> bulkUpdateFieldValues(
            @PathVariable Long clientId,
            @RequestBody Map<String, String> fieldValues) {
        
        log.info("POST /api/clients/{}/fields/bulk - Bulk updating {} fields", clientId, fieldValues.size());
        
        fieldValueService.bulkUpdateFieldValues(clientId, fieldValues);
        Map<String, String> updatedValues = fieldValueService.getClientFieldValuesAsMap(clientId);
        
        return ResponseEntity.ok(updatedValues);
    }

    @DeleteMapping("/{fieldKey}")
    public ResponseEntity<Void> deleteFieldValue(
            @PathVariable Long clientId,
            @PathVariable String fieldKey) {
        
        log.info("DELETE /api/clients/{}/fields/{} - Deleting field value", clientId, fieldKey);
        
        fieldValueService.deleteFieldValue(clientId, fieldKey);
        return ResponseEntity.noContent().build();
    }
}
