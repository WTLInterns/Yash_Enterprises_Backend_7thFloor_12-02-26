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
    
    @Modifying
    @Transactional
    @Query("UPDATE AppNotification n SET n.readAt = :readAt WHERE n.recipientEmployeeId = :employeeId AND n.readAt IS NULL")
    void markAllAsRead(@Param("employeeId") Long employeeId, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AppNotification n WHERE n.recipientEmployeeId = :employeeId")
    void deleteAllByRecipientEmployeeId(@Param("employeeId") Long employeeId);
}
