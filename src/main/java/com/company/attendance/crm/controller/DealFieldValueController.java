package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.DealFieldValue;
import com.company.attendance.crm.service.DealFieldValueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Tag(name = "Deal Field Values")
@RestController
@RequestMapping("/api/deals/{dealId}/fields")
public class DealFieldValueController {
    private final DealFieldValueService service;

    public DealFieldValueController(DealFieldValueService service) {
        this.service = service;
    }

    @GetMapping
    public List<DealFieldValue> list(@PathVariable Long dealId) {
        return service.list(dealId);
    }

    @PostMapping
    public ResponseEntity<DealFieldValue> upsert(@PathVariable Long dealId, @RequestBody Map<String, String> payload) {
        String fieldKey = payload.get("fieldKey");
        String value = payload.get("value");
        DealFieldValue saved = service.upsert(dealId, fieldKey, value);
        return ResponseEntity.created(URI.create("/api/deals/" + dealId + "/fields/" + saved.getId())).body(saved);
    }
}
