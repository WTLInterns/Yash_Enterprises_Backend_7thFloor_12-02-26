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
    
    List<Task> findByAssignedToEmployeeId(Long employeeId);

    List<Task> findByClientId(Long clientId);

    boolean existsByCustomerAddressId(Long customerAddressId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Task t WHERE t.clientId IN :ids")
    void deleteAllByClientIdIn(@org.springframework.data.repository.query.Param("ids") List<Long> ids);
    
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.client
        LEFT JOIN FETCH t.assignedToEmployee ae
        LEFT JOIN FETCH ae.role
        LEFT JOIN FETCH ae.department
        LEFT JOIN FETCH t.createdByEmployee
        LEFT JOIN FETCH t.customerAddress
        WHERE t.assignedToEmployeeId = :employeeId
        ORDER BY t.id DESC
        """)
    List<Task> findByAssignedToEmployeeIdOrderByIdDesc(@Param("employeeId") Long employeeId);

    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.client
        LEFT JOIN FETCH t.assignedToEmployee ae
        LEFT JOIN FETCH ae.role
        LEFT JOIN FETCH ae.department
        LEFT JOIN FETCH t.createdByEmployee
        LEFT JOIN FETCH t.customerAddress
        WHERE t.clientId = :clientId AND t.assignedToEmployeeId = :assignedToEmployeeId
        ORDER BY t.id DESC
        """)
    List<Task> findByClientIdAndAssignedToEmployeeIdOrderByIdDesc(
        @Param("clientId") Long clientId,
        @Param("assignedToEmployeeId") Long assignedToEmployeeId
    );
    
    List<Task> findByDepartment(String department);
    
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.client
        LEFT JOIN FETCH t.assignedToEmployee ae
        LEFT JOIN FETCH ae.role
        LEFT JOIN FETCH ae.department
        LEFT JOIN FETCH t.createdByEmployee
        LEFT JOIN FETCH t.customerAddress
        WHERE t.department = :department
        ORDER BY t.id DESC
        """)
    List<Task> findByDepartmentOrderByIdDesc(@Param("department") String department);

    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.client
        LEFT JOIN FETCH t.assignedToEmployee ae
        LEFT JOIN FETCH ae.role
        LEFT JOIN FETCH ae.department
        LEFT JOIN FETCH t.createdByEmployee
        LEFT JOIN FETCH t.customerAddress
        LEFT JOIN FETCH t.customFieldValues
        ORDER BY t.id DESC
        """)
    List<Task> findAllWithRelations();
    
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.client
        LEFT JOIN FETCH t.assignedToEmployee ae
        LEFT JOIN FETCH ae.role
        LEFT JOIN FETCH ae.department
        LEFT JOIN FETCH t.createdByEmployee
        LEFT JOIN FETCH t.customerAddress
        WHERE t.createdByEmployeeId = :tlId
          AND ae.department = :department
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
    List<Task> findActiveTasksByEmployeeId(@Param("employeeId") Long employeeId);
    
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

