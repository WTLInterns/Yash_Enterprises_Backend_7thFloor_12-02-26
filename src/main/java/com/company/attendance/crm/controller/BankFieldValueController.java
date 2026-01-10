package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.BankFieldValue;
import com.company.attendance.crm.service.BankFieldValueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Bank Field Values")
@RestController
@RequestMapping("/api/banks/{bankId}/fields")
public class BankFieldValueController {
    private final BankFieldValueService service;

    public BankFieldValueController(BankFieldValueService service) { this.service = service; }

    @GetMapping
    public List<BankFieldValue> list(@PathVariable UUID bankId){ return service.list(bankId); }

    @PostMapping
    public ResponseEntity<BankFieldValue> upsert(@PathVariable UUID bankId, @RequestBody Map<String, String> body){
        String fieldKey = body.get("fieldKey");
        String value = body.get("value");
        return ResponseEntity.ok(service.upsert(bankId, fieldKey, value));
    }
}
