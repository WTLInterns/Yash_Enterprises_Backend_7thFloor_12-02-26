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
     * Find all tasks assigned to an employee ordered by ID desc
     */
    List<Task> findByAssignedToEmployeeIdOrderByIdDesc(Long employeeId);

    List<Task> findByClientIdAndAssignedToEmployeeIdOrderByIdDesc(
        Long clientId,
        Long assignedToEmployeeId
    );
    
    /**
     * 🔥 DEPARTMENT-AWARE: Find all tasks in a department
     */
    List<Task> findByDepartment(String department);
    
    /**
     * 🔥 DEPARTMENT-AWARE: Find all tasks in a department ordered by ID desc
     */
    List<Task> findByDepartmentOrderByIdDesc(String department);
    
    /**
     * 🔥 DEPARTMENT-AWARE: Find all tasks created by a TL in their department
     */
    @Query("""
        SELECT t FROM Task t 
        JOIN t.assignedToEmployee e 
        WHERE t.createdByEmployeeId = :tlId 
          AND e.department = :department 
        ORDER BY t.id DESC
        """)
    List<Task> findTasksCreatedByTlInDepartment(
        @Param("tlId") Long tlId, 
        @Param("department") String department
    );
    
    /**
     * Find active task for employee (INQUIRY, IN_PROGRESS, ASSIGNED status) with customer address
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.customerAddress WHERE t.assignedToEmployeeId = :employeeId AND t.status IN ('INQUIRY', 'IN_PROGRESS', 'ASSIGNED') ORDER BY t.scheduledStartTime DESC")
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

