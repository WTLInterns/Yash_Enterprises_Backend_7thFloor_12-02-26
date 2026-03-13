package com.company.attendance.service;

import com.company.attendance.dto.AttendanceDto;
import com.company.attendance.entity.Attendance;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.entity.LeaveRequest;
import com.company.attendance.mapper.AttendanceMapper;
import com.company.attendance.repository.AttendanceRepository;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.EmployeeTrackingRepository;
import com.company.attendance.repository.HolidayRepository;
import com.company.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.company.attendance.util.DistanceCalculator.distanceMeters;

/**
 * CRITICAL RULE: Single Source of Truth
 * 
 * employee_punch = source of truth
 * attendance = derived / summary table
 * 
 * WARNING: attendance records must ONLY be generated
 *    from employee_punch aggregation
 * 
 * NEVER write directly to attendance
 * NEVER punch without task
 * ALWAYS derive from employee_punch
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final EmployeePunchRepository employeePunchRepository;
    private final EmployeeTrackingRepository employeeTrackingRepository;
    private final HolidayRepository holidayRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    private static final LocalTime PRESENT_CUTOFF = LocalTime.of(10, 0);

    /**
     * DERIVED ONLY - Generate attendance from employee_punch
     * This is the ONLY way attendance should be created
     */
    public Attendance generateFromPunch(EmployeePunch punch) {
        if (punch == null || punch.getEmployee() == null) {
            throw new IllegalArgumentException("Cannot generate attendance from null punch");
        }
        
        LocalDate date = punch.getPunchInTime().toLocalDate();
        
        // For now, create a simple attendance record without checking for existing ones
        // TODO: Implement findByEmployeeIdAndDate method in AttendanceRepository
        Attendance attendance = new Attendance();
        attendance.setEmployee(punch.getEmployee());
        attendance.setDate(punch.getPunchInTime().toLocalDate());
        attendance.setPunchInTime(punch.getPunchInTime() != null ? punch.getPunchInTime().atOffset(java.time.ZoneOffset.UTC) : null);
        attendance.setPunchOutTime(punch.getPunchOutTime() != null ? punch.getPunchOutTime().atOffset(java.time.ZoneOffset.UTC) : null);
        attendance.setInLocationLat(punch.getLatitude() != null ? java.math.BigDecimal.valueOf(punch.getLatitude()) : null);
        attendance.setInLocationLng(punch.getLongitude() != null ? java.math.BigDecimal.valueOf(punch.getLongitude()) : null);
        
        // Set status based on business rules
        attendance.setStatus(resolveStatus(punch.getEmployee().getId(), date, punch, attendance));
        
        return attendanceRepository.save(attendance);
    }

    public AttendanceDto getTodaySummary(Long employeeId) {
        LocalDate today = LocalDate.now();
        var attendance = attendanceRepository.findFirstByEmployee_IdAndDate(employeeId, today)
                .orElse(null);

        if (attendance == null) {
            AttendanceDto dto = new AttendanceDto();
            dto.setEmployeeId(employeeId);
            dto.setDate(today);
            dto.setStatus(resolveNoPunchStatus(employeeId, today).toString());
            dto.setTotalHours(BigDecimal.ZERO);
            dto.setTotalKm(BigDecimal.ZERO);
            return dto;
        }

        AttendanceDto dto = toDtoEnriched(attendance);
        if (dto.getTotalHours() == null) dto.setTotalHours(BigDecimal.ZERO);
        if (dto.getTotalKm() == null) dto.setTotalKm(BigDecimal.ZERO);
        return dto;
    }

    public AttendanceDto toDtoEnriched(Attendance attendance) {
        AttendanceDto dto = attendanceMapper.toDto(attendance);
        enrichPunchAddresses(dto);
        return dto;
    }

    public List<AttendanceDto> toDtosEnriched(List<Attendance> attendances) {
        return attendances.stream().map(this::toDtoEnriched).collect(Collectors.toList());
    }

    private void enrichPunchAddresses(AttendanceDto dto) {
        if (dto == null || dto.getEmployeeId() == null || dto.getDate() == null) return;

        try {
            List<EmployeePunch> punches = employeePunchRepository.findByEmployeeIdAndDate(
                    dto.getEmployeeId(),
                    dto.getDate());
            if (punches == null || punches.isEmpty()) return;

            EmployeePunch in = punches.stream()
                    .filter(p -> p.getPunchType() != null && "IN".equalsIgnoreCase(p.getPunchType()))
                    .findFirst()
                    .orElse(null);
            EmployeePunch out = punches.stream()
                    .filter(p -> p.getPunchType() != null && "OUT".equalsIgnoreCase(p.getPunchType()))
                    .reduce((first, second) -> second)
                    .orElse(null);

            if (in != null) dto.setPunchInAddress(in.getLocationAddress());
            if (out != null) dto.setPunchOutAddress(out.getLocationAddress());
        } catch (Exception ignored) {
            // Defensive: do not fail attendance response due to enrichment
        }
    }

    private BigDecimal computeTrackingKm(Long employeeId, LocalDateTime start, LocalDateTime end) {
        try {
            var points = employeeTrackingRepository.findByEmployeeIdAndTimestampBetweenAsc(employeeId, start, end);
            if (points == null || points.size() < 2) return null;

            double meters = 0.0;
            for (int i = 1; i < points.size(); i++) {
                var prev = points.get(i - 1);
                var cur = points.get(i);
                meters += distanceMeters(prev.getLatitude(), prev.getLongitude(), cur.getLatitude(), cur.getLongitude());
            }

            BigDecimal km = BigDecimal.valueOf(meters)
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            if (km.compareTo(BigDecimal.ZERO) < 0) return null;
            return km;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Upsert derived attendance from a legacy EmployeePunch event.
     *
     * IMPORTANT: employee_punch remains the source-of-truth.
     * attendance is just a derived summary table.
     */
    public Attendance upsertFromLegacyPunchEvent(EmployeePunch punch) {
        if (punch == null || punch.getEmployee() == null) {
            throw new IllegalArgumentException("Cannot upsert attendance from null punch");
        }
        if (punch.getPunchTime() == null || punch.getPunchType() == null) {
            throw new IllegalArgumentException("Punch must have punchTime and punchType");
        }

        final LocalDate date = punch.getPunchTime().toLocalDate();
        final Long employeeId = punch.getEmployee().getId();

        Attendance attendance = attendanceRepository
                .findFirstByEmployee_IdAndDate(employeeId, date)
                .orElseGet(() -> {
                    Attendance a = new Attendance();
                    a.setEmployee(punch.getEmployee());
                    a.setDate(date);
                    return a;
                });

        if ("IN".equalsIgnoreCase(punch.getPunchType())) {
            if (attendance.getPunchInTime() == null) {
                attendance.setPunchInTime(punch.getPunchTime().atOffset(ZoneOffset.UTC));
            }
            if (attendance.getInLocationLat() == null && punch.getLatitude() != null) {
                attendance.setInLocationLat(BigDecimal.valueOf(punch.getLatitude()));
            }
            if (attendance.getInLocationLng() == null && punch.getLongitude() != null) {
                attendance.setInLocationLng(BigDecimal.valueOf(punch.getLongitude()));
            }

            // Attendance status: resolved via business rules
            attendance.setStatus(resolveStatus(employeeId, date, punch, attendance));
        }

        if ("OUT".equalsIgnoreCase(punch.getPunchType())) {
            attendance.setPunchOutTime(punch.getPunchTime().atOffset(ZoneOffset.UTC));
            if (punch.getLatitude() != null) {
                attendance.setOutLocationLat(BigDecimal.valueOf(punch.getLatitude()));
            }
            if (punch.getLongitude() != null) {
                attendance.setOutLocationLng(BigDecimal.valueOf(punch.getLongitude()));
            }

            // totalHours
            if (attendance.getPunchInTime() != null) {
                Duration d = Duration.between(attendance.getPunchInTime(), attendance.getPunchOutTime());
                BigDecimal hours = BigDecimal.valueOf(d.toMinutes())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                attendance.setTotalHours(hours);
            }

            // totalKm (prefer tracking-based sum; fallback to straight-line)
            if (attendance.getPunchInTime() != null && attendance.getPunchOutTime() != null) {
                BigDecimal trackingKm = computeTrackingKm(
                        employeeId,
                        attendance.getPunchInTime().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                        attendance.getPunchOutTime().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());

                if (trackingKm != null) {
                    attendance.setTotalKm(trackingKm);
                }
            }

            if (attendance.getTotalKm() == null
                    && attendance.getInLocationLat() != null && attendance.getInLocationLng() != null
                    && attendance.getOutLocationLat() != null && attendance.getOutLocationLng() != null) {
                double meters = distanceMeters(
                        attendance.getInLocationLat().doubleValue(),
                        attendance.getInLocationLng().doubleValue(),
                        attendance.getOutLocationLat().doubleValue(),
                        attendance.getOutLocationLng().doubleValue());

                if (meters != Double.MAX_VALUE) {
                    BigDecimal km = BigDecimal.valueOf(meters)
                            .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
                    attendance.setTotalKm(km);
                }
            }

            // Re-resolve business status with latest state (hours/km already computed above)
            attendance.setStatus(resolveStatus(employeeId, date, punch, attendance));
        }

        return attendanceRepository.save(attendance);
    }

    public Attendance markAbsentIfNotExists(com.company.attendance.entity.Employee employee, LocalDate date) {
        Attendance attendance = attendanceRepository
                .findFirstByEmployee_IdAndDate(employee.getId(), date)
                .orElseGet(() -> {
                    Attendance a = new Attendance();
                    a.setEmployee(employee);
                    a.setDate(date);
                    return a;
                });

        // Noon scheduler rule: if there is no punch yet, set holiday/weeklyOff/leave first, otherwise absent.
        if (attendance.getPunchInTime() == null && attendance.getPunchOutTime() == null) {
            attendance.setStatus(resolveNoPunchStatus(employee.getId(), date));
        } else if (attendance.getStatus() == null) {
            // Defensive fallback
            attendance.setStatus(Attendance.Status.PRESENT);
        }
        return attendanceRepository.save(attendance);
    }

    private Attendance.Status determineStatusFromPunchInTime(LocalTime punchInTime) {
        if (punchInTime == null) {
            return Attendance.Status.PRESENT;
        }
        return punchInTime.isAfter(PRESENT_CUTOFF) ? Attendance.Status.HALF_DAY : Attendance.Status.PRESENT;
    }

    private Attendance.Status resolveNoPunchStatus(Long employeeId, LocalDate date) {
        if (isHoliday(date)) return Attendance.Status.HOLIDAY;
        if (isWeeklyOff(date)) return Attendance.Status.WEEKLY_OFF;
        if (isOnApprovedLeave(employeeId, date)) return Attendance.Status.ON_LEAVE;
        return Attendance.Status.ABSENT;
    }

    private Attendance.Status resolveStatus(
            Long employeeId,
            LocalDate date,
            EmployeePunch punch,
            Attendance attendance
    ) {
        if (isHoliday(date)) return Attendance.Status.HOLIDAY;
        if (isWeeklyOff(date)) return Attendance.Status.WEEKLY_OFF;
        if (isOnApprovedLeave(employeeId, date)) return Attendance.Status.ON_LEAVE;
        if (isPendingManualPunch(punch)) return Attendance.Status.PENDING;

        LocalTime punchInTime = null;
        if (attendance != null && attendance.getPunchInTime() != null) {
            punchInTime = attendance.getPunchInTime().atZoneSameInstant(ZoneOffset.UTC).toLocalTime();
        } else if (punch != null && punch.getPunchTime() != null) {
            punchInTime = punch.getPunchTime().toLocalTime();
        }

        return determineStatusFromPunchInTime(punchInTime);
    }

    private boolean isPendingManualPunch(EmployeePunch punch) {
        if (punch == null) return false;
        final Boolean manual = punch.getIsManualPunch();
        if (manual == null || !manual) return false;
        return punch.getApprovedBy() == null;
    }

    private boolean isHoliday(LocalDate date) {
        if (date == null) return false;
        try {
            return holidayRepository.existsByIsActiveTrueAndDate(date);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWeeklyOff(LocalDate date) {
        if (date == null) return false;
        return DayOfWeek.SUNDAY.equals(date.getDayOfWeek());
    }

    private boolean isOnApprovedLeave(Long employeeId, LocalDate date) {
        if (employeeId == null || date == null) return false;
        try {
            return leaveRequestRepository
                    .existsByEmployee_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                            employeeId,
                            LeaveRequest.Status.APPROVED,
                            date,
                            date);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * DEPRECATED - Direct save method (should not be used)
     * Kept for backward compatibility but logs warning
     */
    @Deprecated
    public Attendance save(Attendance attendance) {
        log.warn("DIRECT ATTENDANCE SAVE DETECTED - This should only be called from generateFromPunch()");
        if (attendance.getEmployee() != null) {
            log.warn("Employee ID: {}, Date: {}", attendance.getEmployee().getId(), attendance.getDate());
        }
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> findAttendanceOfEmployee(Long employeeId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, from, to);
    }

    public List<Attendance> findByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public List<AttendanceDto> getAttendanceByDateRange(LocalDate from, LocalDate to, Long teamId) {
        List<Attendance> records;
        if (teamId != null) {
            records = attendanceRepository.findByDateBetweenAndTeamId(from, to, teamId);
        } else {
            records = attendanceRepository.findByDateBetween(from, to);
        }
        return records.stream().map(attendanceMapper::toDto).collect(Collectors.toList());
    }

    public List<Attendance> bulkSave(List<Attendance> attendances) {
        return attendanceRepository.saveAll(attendances);
    }

    public Map<String, Object> getDashboardSummary(LocalDate date) {
        Map<String, Object> summary = new HashMap<>();
        var all = attendanceRepository.findByDate(date);
        summary.put("date", date);
        summary.put("totalEmployees", all.size());
        summary.put("punchedIn", all.stream().filter(a -> "PUNCHED_IN".equals(a.getStatus().toString())).count());
        return summary;
    }
}
