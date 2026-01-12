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
    public Page<Activity> list(@PathVariable("dealId") Integer dealId,
                               @RequestParam(value = "type", required = false) ActivityType type,
                               Pageable pageable) {
        return activityService.list(dealId, type, pageable);
    }

    @PostMapping
    public ResponseEntity<Activity> create(@PathVariable("dealId") Integer dealId,
                                           @RequestBody Activity activity,
                                           @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        try {
            Activity created = activityService.create(dealId, activity, userId);
            return ResponseEntity.created(URI.create("/api/deals/" + dealId + "/activities/" + created.getId())).body(created);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ActivityController.class).error("Activity create failed for deal {}", dealId, e);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Activity create failed",
                e
            );
        }
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<Activity> update(@PathVariable Integer dealId,
                           @PathVariable("activityId") Long activityId,
                           @RequestBody Activity incoming) {
        try {
            Activity updated = activityService.update(dealId, activityId, incoming);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ActivityController.class).error("Activity update failed for deal {} activity {}", dealId, activityId, e);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Activity update failed",
                e
            );
        }
    }

    @PatchMapping("/{activityId}/status")
    public ResponseEntity<Activity> patchStatus(@PathVariable("dealId") Integer dealId,
                                @PathVariable("activityId") Long activityId,
                                @RequestParam("status") ActivityStatus status) {
        Activity updated = activityService.patchStatus(dealId, activityId, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(@PathVariable("dealId") Integer dealId,
                                       @PathVariable("activityId") Long activityId) {
        try {
            activityService.delete(dealId, activityId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                e.getMessage(),
                e
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ActivityController.class).error("Activity delete failed for deal {} activity {}", dealId, activityId, e);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Activity delete failed",
                e
            );
        }
    }
}
