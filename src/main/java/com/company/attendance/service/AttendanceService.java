package com.company.attendance.service;

import com.company.attendance.dto.AttendanceDto;
import com.company.attendance.entity.Attendance;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.mapper.AttendanceMapper;
import com.company.attendance.repository.AttendanceRepository;
import com.company.attendance.repository.EmployeePunchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        
        // Set status based on punch
        if (punch.getPunchOutTime() != null) {
            attendance.setStatus(Attendance.Status.PUNCHED_OUT);
        } else {
            attendance.setStatus(punch.getLateMark() ? Attendance.Status.ABSENT : Attendance.Status.PUNCHED_IN);
        }
        
        return attendanceRepository.save(attendance);
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
