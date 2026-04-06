package com.company.attendance.repository;

import com.company.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee_IdOrderByIdDesc(Long employeeId);

    List<LeaveRequest> findByEmployee_Tl_IdOrderByIdDesc(Long tlId);

    List<LeaveRequest> findByEmployee_ReportingManager_IdOrderByIdDesc(Long managerId);

    boolean existsByEmployee_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId,
            LeaveRequest.Status status,
            LocalDate dateStart,
            LocalDate dateEnd
    );
}
