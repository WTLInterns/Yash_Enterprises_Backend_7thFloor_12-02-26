package com.company.attendance.controller;

import com.company.attendance.dto.EmployeePunchDto;
import com.company.attendance.service.EmployeePunchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
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
    public ResponseEntity<EmployeePunchDto> punchOut(@Valid @RequestBody EmployeePunchDto dto) {
        dto.setPunchType("OUT");
        var created = employeePunchService.savePunch(dto);
        return ResponseEntity.ok(employeePunchService.toDto(created));
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
