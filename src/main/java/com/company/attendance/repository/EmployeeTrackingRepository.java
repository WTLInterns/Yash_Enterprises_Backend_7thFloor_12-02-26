package com.company.attendance.repository;

import com.company.attendance.entity.EmployeeTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmployeeTrackingRepository extends JpaRepository<EmployeeTracking, Long> {

    // List<EmployeeTracking> findByEmployeeIdOrderByTimestampDesc(Long employeeId);

    // List<EmployeeTracking> findByEmployeeIdAndTimestampBetweenOrderByTimestampDesc(
    //         Long employeeId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT et FROM EmployeeTracking et WHERE et.timestamp >= :start ORDER BY et.timestamp DESC")
    List<EmployeeTracking> findRecentTracking(@Param("start") LocalDateTime start);

    @Query("SELECT et FROM EmployeeTracking et WHERE et.employee.id = :employeeId AND et.timestamp >= :start ORDER BY et.timestamp DESC")
    List<EmployeeTracking> findRecentTrackingByEmployee(@Param("employeeId") Long employeeId, @Param("start") LocalDateTime start);

    @Query("SELECT et FROM EmployeeTracking et WHERE et.employee.id = :employeeId AND et.timestamp >= :start AND et.timestamp < :end ORDER BY et.timestamp ASC")
    List<EmployeeTracking> findByEmployeeIdAndTimestampBetweenAsc(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT et FROM EmployeeTracking et
            WHERE et.id IN (
                SELECT MAX(e2.id)
                FROM EmployeeTracking e2
                GROUP BY e2.employee.id
            )
            """)
    List<EmployeeTracking> findLatestPerEmployee();

    @Query("SELECT et FROM EmployeeTracking et WHERE et.employee.id = :employeeId ORDER BY et.id DESC")
    List<EmployeeTracking> findLatestForEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT et FROM EmployeeTracking et WHERE et.latitude BETWEEN :latMin AND :latMax AND et.longitude BETWEEN :lonMin AND :lonMax")
    List<EmployeeTracking> findTrackingInArea(@Param("latMin") Double latMin, @Param("latMax") Double latMax, 
                                            @Param("lonMin") Double lonMin, @Param("lonMax") Double lonMax);

    @Query("SELECT et FROM EmployeeTracking et WHERE et.trackingType = :trackingType AND et.timestamp >= :start")
    List<EmployeeTracking> findByTrackingTypeAndTimestampAfter(@Param("trackingType") String trackingType, @Param("start") LocalDateTime start);

    // Latest two records for an employee, used to compute movement/idle status
    List<EmployeeTracking> findTop2ByEmployee_IdOrderByTimestampDesc(Long employeeId);

    @Query("""
            SELECT DISTINCT et.employee.id as employeeId
            FROM EmployeeTracking et 
            WHERE et.employee.id IN (
                SELECT e2.employee.id 
                FROM EmployeeTracking e2 
                GROUP BY e2.employee.id 
                HAVING COUNT(e2.id) >= 2
            )
            """)
    List<Object[]> findEmployeesWithAtLeastTwoTrackingRecords();

    @Query("""
            SELECT 
                et.employee.id as employeeId,
                MAX(et.timestamp) as lastTrackingTime,
                et.latitude as latitude,
                et.longitude as longitude,
                et.locationAddress as address
            FROM EmployeeTracking et 
            WHERE et.timestamp < :cutoffTime
            GROUP BY et.employee.id, et.latitude, et.longitude, et.locationAddress
            HAVING MAX(et.timestamp) < :cutoffTime
            """)
    List<Object[]> findEmployeesWithLastTrackingBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
}
