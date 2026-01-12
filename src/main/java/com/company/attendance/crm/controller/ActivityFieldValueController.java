package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.ActivityFieldValue;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.service.ActivityFieldValueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Activity Field Values")
@RestController
@RequestMapping("/api/activities/{activityId}/fields")
public class ActivityFieldValueController {
    private final ActivityFieldValueService service;

    public ActivityFieldValueController(ActivityFieldValueService service) { this.service = service; }

    @GetMapping
    public List<ActivityFieldValue> list(@PathVariable Integer activityId){ return service.list(activityId); }

    @PostMapping
    public ResponseEntity<ActivityFieldValue> upsert(@PathVariable Integer activityId,
                                                     @RequestParam("type") ActivityType type,
                                                     @RequestBody Map<String, String> body){
        String fieldKey = body.get("fieldKey");
        String value = body.get("value");
        return ResponseEntity.ok(service.upsert(activityId, type, fieldKey, value));
    }
}
