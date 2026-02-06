package com.company.attendance.controller;

import com.company.attendance.dto.EmployeeTrackingDto;
import com.company.attendance.entity.EmployeeIdleEvent;
import com.company.attendance.service.EmployeeTrackingService;
import com.company.attendance.repository.EmployeeIdleEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeTrackingController {
    private final EmployeeTrackingService trackingService;
    private final EmployeeIdleEventRepository idleEventRepository;

    @PostMapping("/location")
    public ResponseEntity<EmployeeTrackingDto> updateLocation(@Valid @RequestBody EmployeeTrackingDto dto) {
        var saved = trackingService.saveTracking(dto);
        return ResponseEntity.ok(trackingService.toDto(saved));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeTrackingDto>> getEmployeeTracking(@PathVariable Long employeeId) {
        var dtos = trackingService.findByEmployeeId(employeeId)
                .stream()
                .map(trackingService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/employee/{employeeId}/recent")
    public ResponseEntity<List<EmployeeTrackingDto>> getRecentEmployeeTracking(
            @PathVariable Long employeeId,
            @RequestParam(required = false) String since
    ) {
        LocalDateTime sinceTs = since != null ? LocalDateTime.parse(since) : LocalDateTime.now().minusHours(24);
        var dtos = trackingService.findRecentByEmployeeId(employeeId, sinceTs)
                .stream()
                .map(trackingService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/idle-events")
    public ResponseEntity<List<EmployeeIdleEvent>> getIdleEvents(
            @RequestParam(required = false) String since
    ) {
        LocalDateTime sinceTs = since != null ? LocalDateTime.parse(since) : LocalDateTime.now().minusDays(1);
        List<EmployeeIdleEvent> events = idleEventRepository.findByStartTimeAfterOrderByStartTimeDesc(sinceTs);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/idle-events/employee/{employeeId}")
    public ResponseEntity<List<EmployeeIdleEvent>> getEmployeeIdleEvents(@PathVariable Long employeeId) {
        List<EmployeeIdleEvent> events = idleEventRepository.findByEmployeeIdOrderByStartTimeDesc(employeeId);
        return ResponseEntity.ok(events);
    }
}
