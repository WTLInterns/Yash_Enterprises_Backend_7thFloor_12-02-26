package com.company.attendance.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    Page<AppNotification> findByRecipientEmployeeIdOrderByCreatedAtDesc(Long recipientEmployeeId, Pageable pageable);
    
    Page<AppNotification> findByRecipientRoleOrderByCreatedAtDesc(String recipientRole, Pageable pageable);
    
    Page<AppNotification> findByRecipientDepartmentOrderByCreatedAtDesc(String recipientDepartment, Pageable pageable);
    
    // 🔥 NEW: Admin notifications - Fetch role + department + direct notifications
    @Query("""
        SELECT n FROM AppNotification n
        WHERE n.recipientRole = :role
           OR n.recipientEmployeeId = :employeeId
        ORDER BY n.createdAt DESC
        """)
    Page<AppNotification> findAdminNotifications(
        @Param("role") String role,
        @Param("employeeId") Long employeeId,
        Pageable pageable
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE AppNotification n SET n.readAt = :readAt WHERE n.recipientEmployeeId = :employeeId AND n.readAt IS NULL")
    void markAllAsRead(@Param("employeeId") Long employeeId, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Transactional
    @Query("UPDATE AppNotification n SET n.readAt = :readAt WHERE n.recipientRole = :role AND n.readAt IS NULL")
    void markAllAsReadByRole(@Param("role") String role, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Transactional
    @Query("UPDATE AppNotification n SET n.readAt = :readAt WHERE n.recipientDepartment = :department AND n.readAt IS NULL")
    void markAllAsReadByDepartment(@Param("department") String department, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AppNotification n WHERE n.recipientEmployeeId = :employeeId")
    void deleteAllByRecipientEmployeeId(@Param("employeeId") Long employeeId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AppNotification n WHERE n.recipientRole = :role OR n.recipientEmployeeId IN (SELECT e.id FROM Employee e WHERE e.role.name = :role)")
    void deleteAllByRecipientRole(@Param("role") String role);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AppNotification n WHERE n.recipientDepartment = :department")
    void deleteAllByRecipientDepartment(@Param("department") String department);
}
