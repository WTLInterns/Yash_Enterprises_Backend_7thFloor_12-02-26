package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.Activity;
import com.company.attendance.crm.enums.ActivityStatus;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.service.ActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/deals/{dealId}/activities")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public Page<Activity> list(@PathVariable("dealId") Long dealId,
                               @RequestParam(value = "type", required = false) ActivityType type,
                               Pageable pageable) {
        return activityService.list(dealId, type, pageable);
    }

    @PostMapping
    public ResponseEntity<Activity> create(@PathVariable("dealId") Long dealId,
                                           @RequestBody Activity activity,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Activity created = activityService.create(dealId, activity, userId);
        return ResponseEntity.created(URI.create("/api/deals/" + dealId + "/activities/" + created.getId())).body(created);
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<Activity> update(@PathVariable Long dealId,
                           @PathVariable("activityId") Long activityId,
                           @RequestBody Activity incoming) {
        Activity updated = activityService.update(dealId, activityId, incoming);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{activityId}/status")
    public ResponseEntity<Activity> patchStatus(@PathVariable("dealId") Long dealId,
                                @PathVariable("activityId") Long activityId,
                                @RequestParam("status") ActivityStatus status) {
        Activity updated = activityService.patchStatus(dealId, activityId, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(@PathVariable("dealId") Long dealId,
                                       @PathVariable("activityId") Long activityId) {
        activityService.delete(dealId, activityId);
        return ResponseEntity.noContent().build();
    }
}
