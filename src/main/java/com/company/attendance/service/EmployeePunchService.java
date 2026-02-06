package com.company.attendance.service;

import com.company.attendance.dto.EmployeePunchDto;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.entity.GeofenceZone;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.GeofenceZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeePunchService {
    private final EmployeePunchRepository punchRepository;
    private final EmployeeRepository employeeRepository;
    private final GeofenceZoneRepository geofenceRepository;

    public EmployeePunch savePunch(EmployeePunchDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Boolean isWithinGeofence = false;
        Long geofenceId = null;
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            List<GeofenceZone> active = geofenceRepository.findByIsActive(true);
            for (GeofenceZone gf : active) {
                if (gf.getLatitude() == null || gf.getLongitude() == null || gf.getRadius() == null) continue;
                double distanceMeters = distanceMeters(dto.getLatitude(), dto.getLongitude(), gf.getLatitude(), gf.getLongitude());
                if (distanceMeters <= gf.getRadius()) {
                    isWithinGeofence = true;
                    geofenceId = gf.getId();
                    break;
                }
            }
        }

        EmployeePunch punch = EmployeePunch.builder()
                .employee(employee)
                .punchType(dto.getPunchType())
                .punchTime(dto.getPunchTime() != null ? dto.getPunchTime() : LocalDateTime.now())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .altitude(dto.getAltitude())
                .accuracy(dto.getAccuracy())
                .locationAddress(dto.getLocationAddress())
                .isWithinGeofence(isWithinGeofence)
                .geofenceId(geofenceId)
                .deviceInfo(dto.getDeviceInfo())
                .ipAddress(dto.getIpAddress())
                .notes(dto.getNotes())
                .isManualPunch(dto.getIsManualPunch() != null ? dto.getIsManualPunch() : false)
                .approvedBy(dto.getApprovedBy())
                .createdAt(LocalDateTime.now())
                .build();

        return punchRepository.save(punch);
    }

    public List<EmployeePunch> findByEmployeeId(Long employeeId) {
        return punchRepository.findRecentPunchesByEmployee(employeeId, LocalDateTime.now().minusDays(30));
    }

    public List<EmployeePunch> findByEmployeeIdAndDate(Long employeeId, LocalDate date) {
        return punchRepository.findByEmployeeIdAndDate(employeeId, date);
    }

    public List<EmployeePunch> findRecentByEmployeeId(Long employeeId, LocalDateTime since) {
        return punchRepository.findRecentPunchesByEmployee(employeeId, since);
    }

    public EmployeePunchDto toDto(EmployeePunch punch) {
        final String geofenceName;
        if (punch.getGeofenceId() != null) {
            geofenceName = geofenceRepository.findById(punch.getGeofenceId()).map(GeofenceZone::getName).orElse(null);
        } else {
            geofenceName = null;
        }

        final String approvedByName;
        if (punch.getApprovedBy() != null) {
            approvedByName = employeeRepository.findById(punch.getApprovedBy())
                    .map(emp -> (emp.getFirstName() != null ? emp.getFirstName() : "") + " " + (emp.getLastName() != null ? emp.getLastName() : ""))
                    .map(String::trim)
                    .orElse(null);
        } else {
            approvedByName = null;
        }

        return EmployeePunchDto.builder()
                .id(punch.getId())
                .employeeId(punch.getEmployee().getId())
                .employeeName(((punch.getEmployee().getFirstName() != null ? punch.getEmployee().getFirstName() : "") + " " + (punch.getEmployee().getLastName() != null ? punch.getEmployee().getLastName() : "")).trim())
                .punchType(punch.getPunchType())
                .punchTime(punch.getPunchTime())
                .latitude(punch.getLatitude())
                .longitude(punch.getLongitude())
                .altitude(punch.getAltitude())
                .accuracy(punch.getAccuracy())
                .locationAddress(punch.getLocationAddress())
                .isWithinGeofence(punch.getIsWithinGeofence())
                .geofenceId(punch.getGeofenceId())
                .geofenceName(geofenceName)
                .deviceInfo(punch.getDeviceInfo())
                .ipAddress(punch.getIpAddress())
                .notes(punch.getNotes())
                .isManualPunch(punch.getIsManualPunch())
                .approvedBy(punch.getApprovedBy())
                .approvedByName(approvedByName)
                .createdAt(punch.getCreatedAt())
                .updatedAt(punch.getUpdatedAt())
                .build();
    }

    private static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
}

// package com.company.attendance.service;

// import com.company.attendance.dto.EmployeePunchDto;
// import com.company.attendance.entity.Employee;
// import com.company.attendance.entity.EmployeePunch;
// import com.company.attendance.entity.GeofenceZone;
// import com.company.attendance.repository.EmployeePunchRepository;
// import com.company.attendance.repository.EmployeeRepository;
// import com.company.attendance.repository.GeofenceZoneRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;

// @Service
// @RequiredArgsConstructor
// @Transactional
// public class EmployeePunchService {
//     private final EmployeePunchRepository punchRepository;
//     private final EmployeeRepository employeeRepository;
//     private final GeofenceZoneRepository geofenceRepository;

//     public EmployeePunch savePunch(EmployeePunchDto dto) {
//         Employee employee = employeeRepository.findById(dto.getEmployeeId())
//                 .orElseThrow(() -> new RuntimeException("Employee not found"));

//         // Check if location is within geofence
//         Boolean isWithinGeofence = false;
//         Long geofenceId = null;
//         if (dto.getLatitude() != null && dto.getLongitude() != null) {
//             List<GeofenceZone> geofences = geofenceRepository.findGeofencesContainingLocation(
//                     dto.getLatitude(), dto.getLongitude());
//             if (!geofences.isEmpty()) {
//                 isWithinGeofence = true;
//                 geofenceId = geofences.get(0).getId();
//             }
//         }

//         EmployeePunch punch = EmployeePunch.builder()
//                 .employee(employee)
//                 .punchType(dto.getPunchType())
//                 .punchTime(dto.getPunchTime() != null ? dto.getPunchTime() : LocalDateTime.now())
//                 .latitude(dto.getLatitude())
//                 .longitude(dto.getLongitude())
//                 .altitude(dto.getAltitude())
//                 .accuracy(dto.getAccuracy())
//                 .locationAddress(dto.getLocationAddress())
//                 .isWithinGeofence(isWithinGeofence)
//                 .geofenceId(geofenceId)
//                 .deviceInfo(dto.getDeviceInfo())
//                 .ipAddress(dto.getIpAddress())
//                 .notes(dto.getNotes())
//                 .isManualPunch(dto.getIsManualPunch() != null ? dto.getIsManualPunch() : false)
//                 .approvedBy(dto.getApprovedBy())
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         return punchRepository.save(punch);
//     }

//     public List<EmployeePunch> findByEmployeeId(Long employeeId) {
//         return punchRepository.findRecentPunchesByEmployee(employeeId, LocalDateTime.now().minusDays(30));
//     }

//     public List<EmployeePunch> findByEmployeeIdAndDateRange(Long employeeId, LocalDateTime start, LocalDateTime end) {
//         return punchRepository.findRecentPunchesByEmployee(employeeId, start);
//     }
    
//     public List<EmployeePunch> findByEmployeeIdAndDate(Long employeeId, LocalDate date) {
//         LocalDateTime startOfDay = date.atStartOfDay();
//         LocalDateTime endOfDay = date.atTime(23, 59, 59);
//         return punchRepository.findRecentPunchesByEmployee(employeeId, startOfDay);
//     }

//     public List<EmployeePunch> findRecentByEmployeeId(Long employeeId, LocalDateTime since) {
//         return punchRepository.findRecentPunchesByEmployee(employeeId, since);
//     }

//     public List<EmployeePunch> findRecentPunches(LocalDateTime since) {
//         return punchRepository.findRecentPunches(since);
//     }

//     public List<EmployeePunch> findPunchesInArea(Double latMin, Double latMax, Double lonMin, Double lonMax) {
//         return punchRepository.findPunchesInArea(latMin, latMax, lonMin, lonMax);
//     }

//     public List<EmployeePunch> findByPunchType(String punchType) {
//         return punchRepository.findByPunchTypeAndTimestampAfter(punchType, LocalDateTime.now().minusHours(24));
//     }

//     public List<EmployeePunch> findPunchesOutsideGeofence(LocalDateTime since) {
//         return punchRepository.findPunchesOutsideGeofence(since);
//     }

//     public EmployeePunchDto toDto(EmployeePunch punch) {
//         final String geofenceName;
//         if (punch.getGeofenceId() != null) {
//             final Long geofenceId = punch.getGeofenceId();
//             geofenceName = geofenceRepository.findById(geofenceId)
//                     .map(gf -> gf.getName())
//                     .orElse(null);
//         } else {
//             geofenceName = null;
//         }

//         final String approvedByName;
//         if (punch.getApprovedBy() != null) {
//             final Long approvedById = punch.getApprovedBy();
//             approvedByName = employeeRepository.findById(approvedById)
//                     .map(emp -> emp.getFirstName() + " " + emp.getLastName())
//                     .orElse(null);
//         } else {
//             approvedByName = null;
//         }

//         return EmployeePunchDto.builder()
//                 .id(punch.getId())
//                 .employeeId(punch.getEmployee().getId())
//                 .employeeName(punch.getEmployee().getFirstName() + " " + punch.getEmployee().getLastName())
//                 .punchType(punch.getPunchType())
//                 .punchTime(punch.getPunchTime())
//                 .latitude(punch.getLatitude())
//                 .longitude(punch.getLongitude())
//                 .altitude(punch.getAltitude())
//                 .accuracy(punch.getAccuracy())
//                 .locationAddress(punch.getLocationAddress())
//                 .isWithinGeofence(punch.getIsWithinGeofence())
//                 .geofenceId(punch.getGeofenceId())
//                 .geofenceName(geofenceName)
//                 .deviceInfo(punch.getDeviceInfo())
//                 .ipAddress(punch.getIpAddress())
//                 .notes(punch.getNotes())
//                 .isManualPunch(punch.getIsManualPunch())
//                 .approvedBy(punch.getApprovedBy())
//                 .approvedByName(approvedByName)
//                 .createdAt(punch.getCreatedAt())
//                 .updatedAt(punch.getUpdatedAt())
//                 .build();
//     }
// }
