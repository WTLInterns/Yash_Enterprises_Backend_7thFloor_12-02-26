package com.company.attendance.repository;

import com.company.attendance.entity.EmployeeIdleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmployeeIdleEventRepository extends JpaRepository<EmployeeIdleEvent, Long> {

    @Query("SELECT e FROM EmployeeIdleEvent e WHERE e.employee.id = :employeeId AND e.startTime >= :start ORDER BY e.startTime DESC")
    List<EmployeeIdleEvent> findByEmployeeIdAndStartTimeAfter(@Param("employeeId") Long employeeId, @Param("start") LocalDateTime start);

    @Query("SELECT e FROM EmployeeIdleEvent e WHERE e.employee.id = :empId AND e.endTime IS NULL ORDER BY e.startTime DESC")
    Optional<EmployeeIdleEvent> findTopByEmployeeIdAndEndTimeIsNullOrderByStartTimeDesc(@Param("empId") Long empId);

    List<EmployeeIdleEvent> findByStartTimeAfterOrderByStartTimeDesc(LocalDateTime start);
    
    List<EmployeeIdleEvent> findByEmployeeIdOrderByStartTimeDesc(Long employeeId);
}
