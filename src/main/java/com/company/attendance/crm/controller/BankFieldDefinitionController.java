package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.BankFieldDefinition;
import com.company.attendance.crm.service.BankFieldDefinitionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Bank Field Definitions")
@RestController
@RequestMapping("/api/bank-fields")
public class BankFieldDefinitionController {
    private final BankFieldDefinitionService service;
    public BankFieldDefinitionController(BankFieldDefinitionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BankFieldDefinition> create(@RequestBody BankFieldDefinition d){
        BankFieldDefinition created = service.create(d);
        return ResponseEntity.created(URI.create("/api/bank-fields/"+created.getId())).body(created);
    }

    @GetMapping
    public List<BankFieldDefinition> list(){ return service.list(); }

    @PutMapping("/{id}")
    public BankFieldDefinition update(@PathVariable Long id, @RequestBody BankFieldDefinition incoming){
        return service.update(id, incoming);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<java.util.Map<String, Object>> validate(@RequestParam("fieldKey") String fieldKey,
                                                                  @RequestParam(value = "value", required = false) String value){
        boolean valid = service.validateValue(fieldKey, value);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("fieldKey", fieldKey);
        resp.put("valid", valid);
        return ResponseEntity.ok(resp);
    }
}
