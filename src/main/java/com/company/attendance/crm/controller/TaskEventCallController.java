package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Tag(name = "Deal Activities")
@RestController
@RequestMapping("/api/deals/{dealId}")
public class TaskEventCallController {
    private static final Logger log = LoggerFactory.getLogger(TaskEventCallController.class);
    private final TaskActivityService taskService;
    private final EventActivityService eventService;
    private final CallActivityService callService;

    public TaskEventCallController(TaskActivityService taskService, EventActivityService eventService, CallActivityService callService) {
        this.taskService = taskService;
        this.eventService = eventService;
        this.callService = callService;
    }

    @PostMapping("/tasks")
    public ResponseEntity<Void> createTask(@PathVariable Integer dealId,
                                          @RequestBody TaskActivity task,
                                          @RequestHeader(value = "X-User-Id", required = false) Integer userId){
        try {
            TaskActivity created = taskService.create(dealId, task, userId);
            return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/activities/"+created.getId())).build();
        } catch (Exception e) {
            log.error("Failed to create task for deal {}", dealId, e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to create task", e);
        }
    }

    @PostMapping("/events")
    public ResponseEntity<Void> createEvent(@PathVariable Integer dealId,
                                           @RequestBody EventActivity ev,
                                           @RequestHeader(value = "X-User-Id", required = false) Integer userId){
        try {
            EventActivity created = eventService.create(dealId, ev, userId);
            return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/activities/"+created.getId())).build();
        } catch (Exception e) {
            log.error("Failed to create event for deal {}", dealId, e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to create event", e);
        }
    }

    @PostMapping("/calls")
    public ResponseEntity<Void> createCall(@PathVariable Integer dealId,
                                          @RequestBody CallActivity call,
                                          @RequestHeader(value = "X-User-Id", required = false) Integer userId){
        try {
            CallActivity created = callService.create(dealId, call, userId);
            return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/activities/"+created.getId())).build();
        } catch (Exception e) {
            log.error("Failed to create call for deal {}", dealId, e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to create call", e);
        }
    }
}
