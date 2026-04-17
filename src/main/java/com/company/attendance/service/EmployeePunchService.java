package com.company.attendance.service;

import com.company.attendance.dto.EmployeePunchDto;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.entity.GeofenceZone;
import com.company.attendance.entity.Task;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.GeofenceZoneRepository;
import com.company.attendance.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeePunchService {
    private final EmployeePunchRepository punchRepository;
    private final EmployeeRepository employeeRepository;
    private final GeofenceZoneRepository geofenceRepository;
    private final TaskRepository taskRepository;
    private final GeocodingService geocodingService;
    private final AttendanceService attendanceService;

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private void autoCloseStaleOpenSessions(Long employeeId, LocalDate today) {
        if (employeeId == null) return;
        try {
            LocalDateTime startOfToday = today.atStartOfDay();
            List<EmployeePunch> staleOpen = punchRepository.findOpenPunchesByEmployeeIdBefore(employeeId, startOfToday);
            if (staleOpen == null || staleOpen.isEmpty()) return;

            LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
            for (EmployeePunch punch : staleOpen) {
                if (punch.getPunchOutTime() != null) continue;
                punch.setPunchOutTime(now);
                punch.setPunchTime(now);
                punch.setPunchType("OUT");
                punch.setUpdatedAt(now);
                punchRepository.save(punch);
                try {
                    attendanceService.upsertFromLegacyPunchEvent(punch);
                } catch (Exception e) {
                    log.warn("Auto-close stale session: attendance update failed for punchId={} employeeId={} taskId={}",
                            punch.getId(),
                            punch.getEmployee() != null ? punch.getEmployee().getId() : null,
                            punch.getTask() != null ? punch.getTask().getId() : null,
                            e);
                }
                log.info("Auto-closed stale punch session punchId={} employeeId={} taskId={} punchInTime={}",
                        punch.getId(),
                        punch.getEmployee() != null ? punch.getEmployee().getId() : null,
                        punch.getTask() != null ? punch.getTask().getId() : null,
                        punch.getPunchInTime());
            }
        } catch (Exception e) {
            log.warn("Failed to auto-close stale open sessions for employeeId={}", employeeId, e);
        }
    }

    public EmployeePunch savePunch(EmployeePunchDto dto) {
        System.out.println("========== DEBUG: PUNCH SAVE START ==========");
        System.out.println("Employee ID: " + dto.getEmployeeId());
        System.out.println("Task ID from DTO: " + dto.getTaskId());
        System.out.println("Punch Type: " + dto.getPunchType());
        
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

        Task linkedTask = null;
        if (dto.getTaskId() != null) {
            linkedTask = taskRepository.findById(dto.getTaskId()).orElse(null);
            System.out.println("Task found: " + (linkedTask != null));
            if (linkedTask != null) {
                System.out.println("Task ID: " + linkedTask.getId());
                System.out.println("Task Name: " + linkedTask.getTaskName());
            }
        } else {
            System.out.println("⚠️ CRITICAL: taskId is NULL in DTO - task will NOT be linked!");
        }

        Double distanceFromCustomer = null;
        if ("IN".equals(dto.getPunchType())) {
            final LocalDate today = LocalDate.now(BUSINESS_ZONE);
            autoCloseStaleOpenSessions(dto.getEmployeeId(), today);

            if (dto.getEmployeeId() != null && dto.getTaskId() != null) {
                var activeTodayAnyTask = punchRepository.findActivePunchesByEmployeeIdAndDate(dto.getEmployeeId(), today);
                if (activeTodayAnyTask != null && !activeTodayAnyTask.isEmpty()) {
                    EmployeePunch active = activeTodayAnyTask.get(0);
                    Long activeTaskId = active.getTask() != null ? active.getTask().getId() : null;
                    if (activeTaskId != null && activeTaskId.equals(dto.getTaskId())) {
                        throw new IllegalStateException("Already punched in for this task. Please punch out first.");
                    }
                    throw new IllegalStateException("Already punched in. Please punch out first.");
                }

                var activeTodaySameTask = punchRepository.findActivePunchesByEmployeeIdAndTaskIdAndDate(dto.getEmployeeId(), dto.getTaskId(), today);
                if (activeTodaySameTask != null && !activeTodaySameTask.isEmpty()) {
                    throw new IllegalStateException("Already punched in for this task. Please punch out first.");
                }
            }
            if (linkedTask == null || linkedTask.getCustomerAddressId() == null) {
                throw new RuntimeException("Task location not configured");
            }
            var customerAddress = linkedTask.getCustomerAddress();
            if (customerAddress == null) {
                throw new RuntimeException("Task address not available");
            }
            if (customerAddress.getLatitude() == null || customerAddress.getLongitude() == null) {
                throw new RuntimeException("Task location coordinates not available");
            }
            if (dto.getLatitude() == null || dto.getLongitude() == null) {
                throw new RuntimeException("Latitude/Longitude required");
            }
            double distanceMeters = distanceMeters(
                    dto.getLatitude(), dto.getLongitude(),
                    customerAddress.getLatitude(), customerAddress.getLongitude());
            distanceFromCustomer = distanceMeters;
            if (distanceMeters > 200.0) {
                throw new RuntimeException("Not within 200 meters of task location");
            }
        }

        // Determine punch times based on punch type
        LocalDateTime punchInTime = null;
        LocalDateTime punchOutTime = null;
        
        if ("IN".equals(dto.getPunchType())) {
            punchInTime = dto.getPunchTime() != null ? dto.getPunchTime() : LocalDateTime.now();
            System.out.println("Setting punchInTime: " + punchInTime);
        } else if ("OUT".equals(dto.getPunchType())) {
            punchOutTime = dto.getPunchTime() != null ? dto.getPunchTime() : LocalDateTime.now();
            System.out.println("Setting punchOutTime: " + punchOutTime);
        }

        EmployeePunch punch = EmployeePunch.builder()
                .employee(employee)
                .task(linkedTask)
                .punchInTime(punchInTime)
                .punchOutTime(punchOutTime)
                .attendanceStatus(dto.getAttendanceStatus())
                .punchType(dto.getPunchType())
                .punchTime(dto.getPunchTime() != null ? dto.getPunchTime() : LocalDateTime.now())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .altitude(dto.getAltitude())
                .accuracy(dto.getAccuracy())
                .locationAddress(resolveAddress(dto.getLocationAddress(), dto.getLatitude(), dto.getLongitude()))
                .isWithinGeofence(isWithinGeofence)
                .geofenceId(geofenceId)
                .deviceInfo(dto.getDeviceInfo())
                .ipAddress(dto.getIpAddress())
                .notes(dto.getNotes())
                .isManualPunch(dto.getIsManualPunch() != null ? dto.getIsManualPunch() : false)
                .approvedBy(dto.getApprovedBy())
                .createdAt(LocalDateTime.now())
                .build();

        if (distanceFromCustomer != null) {
            punch.setDistanceFromCustomer(distanceFromCustomer);
        }
        if (dto.getAttendanceStatus() != null) {
            punch.setLateMark("LATE".equalsIgnoreCase(dto.getAttendanceStatus()));
        }

        EmployeePunch saved = punchRepository.save(punch);
        
        System.out.println("Punch saved with ID: " + saved.getId());
        System.out.println("Task in saved punch: " + (saved.getTask() != null ? saved.getTask().getId() : "NULL"));
        System.out.println("punchInTime in saved punch: " + saved.getPunchInTime());
        System.out.println("========== DEBUG: PUNCH SAVE END ==========");

        // Derived attendance update (summary table)
        try {
            attendanceService.upsertFromLegacyPunchEvent(saved);
        } catch (Exception ignored) {
            // Do not fail punch if attendance derivation fails
        }

        return saved;
    }

    private String resolveAddress(String provided, Double latitude, Double longitude) {
        if (provided != null && !provided.isBlank()) {
            return provided;
        }
        if (latitude == null || longitude == null) {
            return null;
        }
        return geocodingService.reverseGeocode(latitude, longitude);
    }

    public List<EmployeePunch> findByEmployeeId(Long employeeId) {
        return punchRepository.findRecentPunchesByEmployee(employeeId, LocalDateTime.now().minusDays(30));
    }

    public EmployeePunch closePunchSession(EmployeePunchDto dto) {
        Long sessionId = dto.getSessionId();
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId is required for punch out");
        }
        EmployeePunch punch = punchRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Punch session not found: " + sessionId));

        if (punch.getPunchOutTime() != null) {
            log.info("Punch-out called for already closed sessionId={} employeeId={} taskId={} existingPunchOutTime={}",
                    sessionId,
                    punch.getEmployee() != null ? punch.getEmployee().getId() : null,
                    punch.getTask() != null ? punch.getTask().getId() : null,
                    punch.getPunchOutTime());
            return punch;
        }

        LocalDateTime punchOutTime = dto.getPunchTime() != null ? dto.getPunchTime() : LocalDateTime.now();
        punch.setPunchOutTime(punchOutTime);
        punch.setPunchTime(punchOutTime);
        punch.setPunchType("OUT");
        if (dto.getLatitude() != null) punch.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) punch.setLongitude(dto.getLongitude());
        if (dto.getAltitude() != null) punch.setAltitude(dto.getAltitude());
        if (dto.getAccuracy() != null) punch.setAccuracy(dto.getAccuracy());
        if (dto.getDeviceInfo() != null) punch.setDeviceInfo(dto.getDeviceInfo());
        punch.setUpdatedAt(LocalDateTime.now());

        EmployeePunch saved = punchRepository.save(punch);
        try {
            attendanceService.upsertFromLegacyPunchEvent(saved);
        } catch (Exception ignored) {}

        log.info("Punch-out successful sessionId={} employeeId={} taskId={} punchOutTime={}",
                saved.getId(),
                saved.getEmployee() != null ? saved.getEmployee().getId() : null,
                saved.getTask() != null ? saved.getTask().getId() : null,
                saved.getPunchOutTime());
        return saved;
    }

    public java.util.Optional<EmployeePunch> findActiveSession(Long employeeId) {
        final LocalDate today = LocalDate.now(BUSINESS_ZONE);
        autoCloseStaleOpenSessions(employeeId, today);
        List<EmployeePunch> activeToday = punchRepository.findActivePunchesByEmployeeIdAndDate(employeeId, today);
        if (activeToday == null || activeToday.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(activeToday.get(0));
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
