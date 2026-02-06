package com.company.attendance.repository;

import com.company.attendance.entity.EmployeePunch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePunchRepository extends JpaRepository<EmployeePunch, Long> {

    // New methods for task-based punch system
    
    /**
     * Find all active punches for employee (no punch_out_time)
     */
    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.employee.id = :employeeId AND ep.punchOutTime IS NULL ORDER BY ep.punchInTime DESC")
    List<EmployeePunch> findActivePunchesByEmployeeId(@Param("employeeId") Long employeeId);
    
    /**
     * Find all active punches (for auto punch-out)
     */
    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.punchOutTime IS NULL")
    List<EmployeePunch> findAllActivePunches();
    
    /**
     * Find punch by employee and task
     */
    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.employee.id = :employeeId AND ep.task.id = :taskId ORDER BY ep.punchInTime DESC")
    List<EmployeePunch> findByEmployeeIdAndTaskId(@Param("employeeId") Long employeeId, @Param("taskId") Long taskId);
    
    /**
     * Find punch by employee and task on specific date
     */
    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.employee.id = :employeeId AND ep.task.id = :taskId AND DATE(ep.punchInTime) = :date")
    Optional<EmployeePunch> findByEmployeeIdAndTaskIdAndDate(@Param("employeeId") Long employeeId, @Param("taskId") Long taskId, @Param("date") LocalDate date);

    // Legacy methods for backward compatibility
    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.employee.id = :employeeId AND DATE(ep.punchTime) = :date ORDER BY ep.punchTime ASC")
    List<EmployeePunch> findByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.punchTime >= :start ORDER BY ep.punchTime DESC")
    List<EmployeePunch> findRecentPunches(@Param("start") LocalDateTime start);

    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.employee.id = :employeeId AND ep.punchTime >= :start ORDER BY ep.punchTime DESC")
    List<EmployeePunch> findRecentPunchesByEmployee(@Param("employeeId") Long employeeId, @Param("start") LocalDateTime start);

    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.latitude BETWEEN :latMin AND :latMax AND ep.longitude BETWEEN :lonMin AND :lonMax")
    List<EmployeePunch> findPunchesInArea(@Param("latMin") Double latMin, @Param("latMax") Double latMax, 
                                         @Param("lonMin") Double lonMin, @Param("lonMax") Double lonMax);

    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.punchType = :punchType AND ep.punchTime >= :start")
    List<EmployeePunch> findByPunchTypeAndTimestampAfter(@Param("punchType") String punchType, @Param("start") LocalDateTime start);

    @Query("SELECT ep FROM EmployeePunch ep WHERE ep.isWithinGeofence = false AND ep.punchTime >= :start")
    List<EmployeePunch> findPunchesOutsideGeofence(@Param("start") LocalDateTime start);
}
