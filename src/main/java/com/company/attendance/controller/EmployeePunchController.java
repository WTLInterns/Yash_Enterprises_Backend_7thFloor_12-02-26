package com.company.attendance.controller;

import com.company.attendance.dto.EmployeePunchDto;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.service.EmployeePunchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/punch")
@RequiredArgsConstructor
public class EmployeePunchController {
    private final EmployeePunchService employeePunchService;

    @PostMapping("/in")
    public ResponseEntity<EmployeePunchDto> punchIn(@Valid @RequestBody EmployeePunchDto dto) {
        dto.setPunchType("IN");
        var created = employeePunchService.savePunch(dto);
        return ResponseEntity.ok(employeePunchService.toDto(created));
    }

    @PostMapping("/out")
    public ResponseEntity<EmployeePunchDto> punchOut(@RequestBody EmployeePunchDto dto) {
        dto.setPunchType("OUT");
        var updated = employeePunchService.closePunchSession(dto);
        return ResponseEntity.ok(employeePunchService.toDto(updated));
    }

    /**
     * Returns the active (not yet punched-out) punch session for an employee.
     * Flutter PunchController._init() calls this on app start to restore session state.
     * Returns 204 No Content when no active session exists.
     */
    @GetMapping("/active/{employeeId}")
    public ResponseEntity<Map<String, Object>> getActiveSession(@PathVariable Long employeeId) {
        return employeePunchService.findActiveSession(employeeId)
                .map(punch -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("sessionId", punch.getId().toString());
                    body.put("punchInTime", punch.getPunchInTime() != null
                            ? punch.getPunchInTime().toString() : null);
                    body.put("taskId", punch.getTask() != null
                            ? punch.getTask().getId() : null);
                    body.put("status", "ACTIVE");
                    body.put("elapsedSeconds",
                            punch.getPunchInTime() != null
                            ? java.time.Duration.between(
                                    punch.getPunchInTime(),
                                    java.time.LocalDateTime.now()).getSeconds()
                            : 0);
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeePunchDto>> listByEmployee(@PathVariable Long employeeId) {
        var punches = employeePunchService.findByEmployeeId(employeeId)
                .stream()
                .map(employeePunchService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(punches);
    }

    @GetMapping("/employee/{employeeId}/date")
    public ResponseEntity<List<EmployeePunchDto>> listByEmployeeAndDate(
            @PathVariable Long employeeId,
            @RequestParam LocalDate date
    ) {
        var punches = employeePunchService.findByEmployeeIdAndDate(employeeId, date)
                .stream()
                .map(employeePunchService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(punches);
    }
}
