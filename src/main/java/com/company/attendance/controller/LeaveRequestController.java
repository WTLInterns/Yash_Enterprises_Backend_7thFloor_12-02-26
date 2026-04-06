package com.company.attendance.controller;

import com.company.attendance.dto.LeaveRequestDto;
import com.company.attendance.entity.LeaveRequest;
import com.company.attendance.mapper.LeaveRequestMapper;
import com.company.attendance.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;
    private final LeaveRequestMapper leaveRequestMapper;

    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDto>> myLeaves() {
        var leaves = leaveRequestService.findMyLeaves();
        var dtos = leaves.stream().map(leaveRequestMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/tl")
    @PreAuthorize("hasRole('TL')")
    public ResponseEntity<List<LeaveRequestDto>> tlLeaves() {
        var leaves = leaveRequestService.findTlLeaves();
        var dtos = leaves.stream().map(leaveRequestMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveRequestDto>> managerLeaves() {
        var leaves = leaveRequestService.findManagerLeaves();
        var dtos = leaves.stream().map(leaveRequestMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> allLeaves() {
        var leaves = leaveRequestService.findAllForAdmin();
        var dtos = leaves.stream().map(leaveRequestMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestDto> getLeave(@PathVariable Long id) {
        return leaveRequestService.findById(id)
                .map(leaveRequestMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDto> createLeave(@Valid @RequestBody LeaveRequestDto dto) {
        LeaveRequest leave = leaveRequestMapper.toEntity(dto);
        LeaveRequest saved = leaveRequestService.applyLeave(leave);
        return ResponseEntity.ok(leaveRequestMapper.toDto(saved));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('TL','MANAGER','ADMIN')")
    public ResponseEntity<LeaveRequestDto> approveLeave(@PathVariable Long id) {
        LeaveRequest approved = leaveRequestService.approve(id);
        return ResponseEntity.ok(leaveRequestMapper.toDto(approved));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('TL','MANAGER','ADMIN')")
    public ResponseEntity<LeaveRequestDto> rejectLeave(@PathVariable Long id) {
        LeaveRequest rejected = leaveRequestService.reject(id);
        return ResponseEntity.ok(leaveRequestMapper.toDto(rejected));
    }
}
