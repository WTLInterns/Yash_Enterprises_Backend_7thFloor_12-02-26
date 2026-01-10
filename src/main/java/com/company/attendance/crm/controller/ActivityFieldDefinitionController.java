package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ActivityFieldDefinition;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.service.ActivityFieldDefinitionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Activity Field Definitions")
@RestController
@RequestMapping("/api/activity-fields")
public class ActivityFieldDefinitionController {
    private final ActivityFieldDefinitionService service;

    public ActivityFieldDefinitionController(ActivityFieldDefinitionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ActivityFieldDefinition> create(@RequestBody ActivityFieldDefinition d){
        ActivityFieldDefinition created = service.create(d);
        return ResponseEntity.created(URI.create("/api/activity-fields/"+created.getId())).body(created);
    }

    @GetMapping
    public List<ActivityFieldDefinition> list(@RequestParam(value = "type", required = false) ActivityType type){
        return service.list(type);
    }

    @PutMapping("/{id}")
    public ActivityFieldDefinition update(@PathVariable UUID id, @RequestBody ActivityFieldDefinition incoming){
        return service.update(id, incoming);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<java.util.Map<String, Object>> validate(@RequestParam("type") ActivityType type,
                                                                  @RequestParam("fieldKey") String fieldKey,
                                                                  @RequestParam(value = "value", required = false) String value){
        boolean valid = service.validateValue(type, fieldKey, value);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("fieldKey", fieldKey);
        resp.put("valid", valid);
        return ResponseEntity.ok(resp);
    }
}
