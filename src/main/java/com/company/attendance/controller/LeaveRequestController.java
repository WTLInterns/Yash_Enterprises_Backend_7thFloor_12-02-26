package com.company.attendance.controller;

import com.company.attendance.dto.LeaveRequestDto;
import com.company.attendance.entity.LeaveRequest;
import com.company.attendance.mapper.LeaveRequestMapper;
import com.company.attendance.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;
    private final LeaveRequestMapper leaveRequestMapper;

    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDto>> myLeaves(
            @RequestParam Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(leaveRequestService.findLeavesByEmployeeId(employeeId, month, year));
    }

    @GetMapping("/tl")
    public ResponseEntity<List<LeaveRequestDto>> tlLeaves(@RequestParam Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.findTlLeaves(employeeId));
    }

    @GetMapping("/manager")
    public ResponseEntity<List<LeaveRequestDto>> managerLeaves(@RequestParam Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.findManagerLeaves(employeeId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeaveRequestDto>> allLeaves(@RequestParam Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.findAllForAdmin(employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestDto> getLeave(
            @PathVariable Long id,
            @RequestParam Long employeeId
    ) {
        return ResponseEntity.ok(leaveRequestService.getLeave(id, employeeId));
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDto> createLeave(
            @RequestParam Long employeeId,
            @Valid @RequestBody LeaveRequestDto dto
    ) {
        LeaveRequest leave = leaveRequestMapper.toEntity(dto);
        return ResponseEntity.ok(leaveRequestService.applyLeave(employeeId, leave));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequestDto> updateLeave(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            @Valid @RequestBody LeaveRequestDto dto
    ) {
        LeaveRequest updated = leaveRequestMapper.toEntity(dto);
        return ResponseEntity.ok(leaveRequestService.update(id, employeeId, updated));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDto> approveLeave(
            @PathVariable Long id,
            @RequestParam Long employeeId
    ) {
        return ResponseEntity.ok(leaveRequestService.approve(id, employeeId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestDto> rejectLeave(
            @PathVariable Long id,
            @RequestParam Long employeeId
    ) {
        return ResponseEntity.ok(leaveRequestService.reject(id, employeeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(
            @PathVariable Long id,
            @RequestParam Long employeeId
    ) {
        leaveRequestService.delete(id, employeeId);
        return ResponseEntity.noContent().build();
    }
}
