package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.DealFieldDefinition;
import com.company.attendance.crm.service.DealFieldDefinitionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Deal Field Definitions")
@RestController
@RequestMapping("/api/deal-fields")
public class DealFieldDefinitionController {
    private final DealFieldDefinitionService service;

    public DealFieldDefinitionController(DealFieldDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DealFieldDefinition> create(@RequestBody DealFieldDefinition d) {
        DealFieldDefinition created = service.create(d);
        return ResponseEntity.created(URI.create("/api/deal-fields/" + created.getId())).body(created);
    }

    @GetMapping
    public List<DealFieldDefinition> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public DealFieldDefinition update(@PathVariable Integer id, @RequestBody DealFieldDefinition incoming) {
        return service.update(id, incoming);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<java.util.Map<String, Object>> validate(@RequestParam("fieldKey") String fieldKey,
                                                                  @RequestParam(value = "value", required = false) String value) {
        boolean valid = service.validateValue(fieldKey, value);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("fieldKey", fieldKey);
        resp.put("valid", valid);
        return ResponseEntity.ok(resp);
    }
}
