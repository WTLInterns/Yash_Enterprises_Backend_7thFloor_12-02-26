package com.company.attendance.controller;

import com.company.attendance.dto.AttendanceDto;
import com.company.attendance.entity.Attendance;
import com.company.attendance.mapper.AttendanceMapper;
import com.company.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Attendance REST endpoints
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    @PostMapping("/punch-in")
    public ResponseEntity<AttendanceDto> punchIn(@Valid @RequestBody AttendanceDto dto) {
        Attendance attendance = attendanceMapper.toEntity(dto);
        // Employee will be set by the service layer based on employeeId
        Attendance saved = attendanceService.save(attendance);
        return ResponseEntity.ok(attendanceMapper.toDto(saved));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<AttendanceDto>> listAttendance(@PathVariable("employeeId") Long employeeId,
                                                   @RequestParam LocalDate from,
                                                   @RequestParam LocalDate to) {
        List<Attendance> list = attendanceService.findAttendanceOfEmployee(employeeId, from, to);
        List<AttendanceDto> dtos = attendanceService.toDtosEnriched(list);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/today/{employeeId}")
    public ResponseEntity<AttendanceDto> today(@PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(attendanceService.getTodaySummary(employeeId));
    }
}
