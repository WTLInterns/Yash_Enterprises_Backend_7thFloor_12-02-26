package com.company.attendance.repository;

import com.company.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee_IdOrderByIdDesc(Long employeeId);

    @Query(
            "SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.fromDate <= :endDate " +
            "AND lr.toDate >= :startDate " +
            "ORDER BY lr.id DESC"
    )
    List<LeaveRequest> findByEmployeeIdOverlappingDateRangeOrderByIdDesc(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<LeaveRequest> findByEmployee_Tl_IdOrderByIdDesc(Long tlId);

    List<LeaveRequest> findByEmployee_ReportingManager_IdOrderByIdDesc(Long managerId);

    boolean existsByEmployee_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId,
            LeaveRequest.Status status,
            LocalDate dateStart,
            LocalDate dateEnd
    );

    void deleteByEmployee_Id(Long employeeId);
}
