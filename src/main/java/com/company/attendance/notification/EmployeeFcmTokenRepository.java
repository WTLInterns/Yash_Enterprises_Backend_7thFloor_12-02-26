package com.company.attendance.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeFcmTokenRepository extends JpaRepository<EmployeeFcmToken, Long> {
    
    // Find all tokens for an employee
    List<EmployeeFcmToken> findByEmployeeId(Long employeeId);
    
    // Find all tokens for an employee by platform
    List<EmployeeFcmToken> findByEmployeeIdAndPlatform(Long employeeId, String platform);
    
    // Check if specific token exists for employee
    Optional<EmployeeFcmToken> findByEmployeeIdAndToken(Long employeeId, String token);
    
    // Delete old tokens for employee (cleanup)
    void deleteByEmployeeIdAndPlatform(Long employeeId, String platform);
    
    // Find all active tokens (for bulk notifications)
    @Query("SELECT t FROM EmployeeFcmToken t WHERE t.employeeId IN :employeeIds")
    List<EmployeeFcmToken> findByEmployeeIds(@Param("employeeIds") List<Long> employeeIds);
}
