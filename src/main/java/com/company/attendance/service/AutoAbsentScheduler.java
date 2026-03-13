package com.company.attendance.service;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoAbsentScheduler {

    private final EmployeeRepository employeeRepository;
    private final EmployeePunchRepository employeePunchRepository;
    private final AttendanceService attendanceService;

    /**
     * Run daily at 12:00 PM.
     * If an ACTIVE employee has no punch record today, mark attendance as ABSENT.
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void markAbsentAtNoon() {
        LocalDate today = LocalDate.now();

        List<Employee> employees = employeeRepository.findByStatus(Employee.Status.ACTIVE);
        int marked = 0;

        for (Employee employee : employees) {
            try {
                long punchCount = employeePunchRepository.countByEmployeeIdAndDate(employee.getId(), today);
                if (punchCount == 0) {
                    attendanceService.markAbsentIfNotExists(employee, today);
                    marked++;
                }
            } catch (Exception e) {
                log.warn("Auto-absent failed for employeeId={}", employee.getId(), e);
            }
        }

        log.info("Auto-absent scheduler completed for date={} markedAbsent={}", today, marked);
    }
}
