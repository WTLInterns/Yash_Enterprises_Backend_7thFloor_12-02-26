package com.company.attendance.repository;

import com.company.attendance.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find all tasks assigned to an employee
     */
    List<Task> findByAssignedToEmployeeId(Long employeeId);
    
    /**
     * Find active task for employee (INQUIRY or IN_PROGRESS status)
     */
    @Query("SELECT t FROM Task t WHERE t.assignedToEmployeeId = :employeeId AND t.status IN ('INQUIRY', 'IN_PROGRESS') ORDER BY t.scheduledStartTime DESC")
    Task findActiveTaskByEmployeeId(@Param("employeeId") Long employeeId);
    
    /**
     * Find tasks by employee and status
     */
    @Query("SELECT t FROM Task t WHERE t.assignedToEmployeeId = :employeeId AND t.status IN :statuses")
    List<Task> findByEmployeeIdAndStatusIn(@Param("employeeId") Long employeeId, @Param("statuses") List<String> statuses);
    
    /**
     * Find task with customer address (bypassed for now)
     */
    // @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.customerAddressId IS NOT NULL")
    // Optional<Task> findWithCustomerAddress(@Param("taskId") Long taskId);
    
    /**
     * Find task by ID (simple version)
     */
    Optional<Task> findTaskById(Long taskId);
}

