package com.company.attendance.repository;

import com.company.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    boolean existsByEmployee_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId,
            LeaveRequest.Status status,
            LocalDate dateStart,
            LocalDate dateEnd
    );
}

