package com.company.attendance.service;

import com.company.attendance.dto.EmployeeTrackingDto;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.EmployeeTracking;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.EmployeeTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeTrackingService {
    private final EmployeeTrackingRepository trackingRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeTracking saveTracking(EmployeeTrackingDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        EmployeeTracking tracking = EmployeeTracking.builder()
                .employee(employee)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .altitude(dto.getAltitude())
                .accuracy(dto.getAccuracy())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .locationAddress(dto.getLocationAddress())
                .resolvedAddress(dto.getResolvedAddress())
                .trackingType(dto.getTrackingType())
                .deviceInfo(dto.getDeviceInfo())
                .ipAddress(dto.getIpAddress())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .build();

        return trackingRepository.save(tracking);
    }

    public List<EmployeeTracking> findByEmployeeId(Long employeeId) {
        return trackingRepository.findRecentTrackingByEmployee(employeeId, LocalDateTime.now().minusDays(30));
    }

    public List<EmployeeTracking> findRecentByEmployeeId(Long employeeId, LocalDateTime since) {
        return trackingRepository.findRecentTrackingByEmployee(employeeId, since);
    }

    public EmployeeTrackingDto toDto(EmployeeTracking tracking) {
        return EmployeeTrackingDto.builder()
                .id(tracking.getId())
                .employeeId(tracking.getEmployee().getId())
                .employeeName(((tracking.getEmployee().getFirstName() != null ? tracking.getEmployee().getFirstName() : "") + " " + (tracking.getEmployee().getLastName() != null ? tracking.getEmployee().getLastName() : "")).trim())
                .latitude(tracking.getLatitude())
                .longitude(tracking.getLongitude())
                .altitude(tracking.getAltitude())
                .accuracy(tracking.getAccuracy())
                .timestamp(tracking.getTimestamp())
                .locationAddress(tracking.getLocationAddress())
                .resolvedAddress(tracking.getResolvedAddress())
                .trackingType(tracking.getTrackingType())
                .deviceInfo(tracking.getDeviceInfo())
                .ipAddress(tracking.getIpAddress())
                .isActive(tracking.getIsActive())
                .createdAt(tracking.getCreatedAt())
                .updatedAt(tracking.getUpdatedAt())
                .build();
    }
}
