package com.company.attendance.notification;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final AppNotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeFcmTokenRepository fcmTokenRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    public void notifyEmployeeMobile(Long employeeId, String title, String body, String type, String refType, Long refId, Map<String, String> data) {
        notifyEmployee(employeeId, "MOBILE", title, body, type, refType, refId, data);
    }

    public void notifyEmployeeWeb(Long employeeId, String title, String body, String type, String refType, Long refId, Map<String, String> data) {
        notifyEmployee(employeeId, "WEB", title, body, type, refType, refId, data);
    }

    @Transactional
    public void notifyEmployee(Long employeeId, String channel, String title, String body, String type, String refType, Long refId, Map<String, String> data) {
        try {
            log.info("🔥 [EMPLOYEE NOTIFICATION] notifyEmployee called:");
            log.info("🔥 [EMPLOYEE NOTIFICATION] - EmployeeId: {}", employeeId);
            log.info("🔥 [EMPLOYEE NOTIFICATION] - Channel: {}", channel);
            log.info("🔥 [EMPLOYEE NOTIFICATION] - Title: {}", title);
            log.info("🔥 [EMPLOYEE NOTIFICATION] - Firebase Enabled: {}", firebaseEnabled);

            final AppNotification saved = notificationRepository.save(
                    AppNotification.builder()
                            .recipientEmployeeId(employeeId)
                            .channel(channel)
                            .title(title)
                            .body(body)
                            .type(type)
                            .refType(refType)
                            .refId(refId)
                            .dataJson(data == null ? null : objectMapper.writeValueAsString(data))
                            .build()
            );

            log.info("🔥 [EMPLOYEE NOTIFICATION] Notification saved for employeeId={}, channel={}, type={}", employeeId, channel, type);

            Employee emp = employeeRepository.findById(employeeId).orElse(null);
            if (emp == null) {
                log.warn("🔥 [EMPLOYEE NOTIFICATION] Employee not found with ID: {}", employeeId);
                return;
            }
            
            log.info("🔥 [EMPLOYEE NOTIFICATION] Found employee: {} (ID: {})", emp.getFullName(), emp.getId());

            // 🔥 NEW: Get all tokens for the employee and platform
            List<EmployeeFcmToken> tokens = fcmTokenRepository.findByEmployeeIdAndPlatform(employeeId, channel);
            log.info("🔥 [EMPLOYEE NOTIFICATION] Found {} {} tokens for employeeId={}", tokens.size(), channel, employeeId);
            
            if (tokens.isEmpty()) {
                log.warn("🔥 [EMPLOYEE NOTIFICATION] No {} tokens found for employeeId={} - Employee: {}", channel, employeeId, emp.getFullName());
                return;
            }

            if (!firebaseEnabled) {
                log.warn("🔥 [EMPLOYEE NOTIFICATION] Firebase is disabled - skipping FCM send");
                return;
            }

            // 🔥 NEW: Send to all tokens for this employee/platform
            for (EmployeeFcmToken tokenEntity : tokens) {
                String token = tokenEntity.getToken();
                if (token == null || token.isBlank()) {
                    continue;
                }

                try {
                    Message.Builder builder = Message.builder()
                            .setToken(token)
                            .setNotification(Notification.builder().setTitle(title).setBody(body).build());

                    if (data != null) {
                        builder.putAllData(data);
                    }

                    FirebaseMessaging.getInstance().send(builder.build());
                    log.info("FCM notification sent to employeeId={}, channel={}, tokenId={}", employeeId, channel, tokenEntity.getId());
                } catch (Exception e) {
                    log.warn("FCM send failed (employeeId={}, channel={}, tokenId={}): {}", employeeId, channel, tokenEntity.getId(), e.getMessage());
                    // Optionally delete invalid token
                    try {
                        fcmTokenRepository.delete(tokenEntity);
                        log.info("Deleted invalid FCM token: tokenId={}", tokenEntity.getId());
                    } catch (Exception deleteEx) {
                        log.warn("Failed to delete invalid token: {}", deleteEx.getMessage());
                    }
                    // keep DB notification even if push fails
                }
            }
        } catch (Exception e) {
            log.error("Failed to save notification: {}", e.getMessage());
        }
    }

    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getReadAt() == null) {
                n.setReadAt(LocalDateTime.now());
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Long employeeId) {
        notificationRepository.markAllAsRead(employeeId, LocalDateTime.now());
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotifications(Long employeeId) {
        notificationRepository.deleteAllByRecipientEmployeeId(employeeId);
    }

    /**
     * Send role-based notification with WebSocket support
     */
    /**
     * ✅ FIXED: Send role-based notification — supports BOTH "MANAGER" and "ADMIN"
     * Replace existing sendRoleBasedNotification method with this one
     */
    @Transactional
    public void sendRoleBasedNotification(String role, String title, String message, String type, Long refId) {
        try {
            log.info("� sendRoleBasedNotification: role={}, title={}, type={}", role, title, type);

            // Find all employees with this role
            List<Employee> employees = employeeRepository.findByRole_Name(role);
            log.info("� Found {} employees with role={}", employees.size(), role);

            for (Employee emp : employees) {
                // Save DB notification per employee (with role set)
                AppNotification notification = AppNotification.builder()
                        .recipientEmployeeId(emp.getId())
                        .recipientRole(role)
                        .title(title)
                        .body(message)
                        .type(type)
                        .refType(type)
                        .refId(refId)
                        .channel("WEB")
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notification);

                // Firebase push
                notifyEmployeeWeb(emp.getId(), title, message, type, type, refId, null);
                notifyEmployeeMobile(emp.getId(), title, message, type, type, refId, null);

                log.info("✅ Notified {} employee: {} (ID: {})", role, emp.getFullName(), emp.getId());
            }

            // WebSocket broadcast to role topic
            messagingTemplate.convertAndSend("/topic/notifications/role/" + role, Map.of(
                "title", title,
                "body", message,
                "type", type,
                "refId", refId != null ? refId : 0L,
                "role", role,
                "timestamp", LocalDateTime.now().toString()
            ));

            log.info("✅ sendRoleBasedNotification complete: role={}, {} employees notified", role, employees.size());

        } catch (Exception e) {
            log.error("❌ sendRoleBasedNotification failed for role={}: {}", role, e.getMessage(), e);
        }
    }

    @Transactional
    public void sendDepartmentNotification(String department, String title, String message, String type, Long refId) {
        try {
            log.info("🔥 [DEPARTMENT NOTIFICATION] sendDepartmentNotification called:");
            log.info("🔥 [DEPARTMENT NOTIFICATION] - Department: {}", department);
            log.info("🔥 [DEPARTMENT NOTIFICATION] - Title: {}", title);
            log.info("🔥 [DEPARTMENT NOTIFICATION] - Message: {}", message);
            log.info("🔥 [DEPARTMENT NOTIFICATION] - Type: {}", type);
            log.info("🔥 [DEPARTMENT NOTIFICATION] - RefId: {}", refId);
            log.info("🔥 [DEPARTMENT NOTIFICATION] - Firebase Enabled: {}", firebaseEnabled);

            // 🔥 NEW: Find all employees in this department for Firebase notifications
            List<Employee> departmentEmployees = employeeRepository.findByDepartment_Name(department);
            log.info("🔥 [DEPARTMENT NOTIFICATION] Found {} employees in {} department", departmentEmployees.size(), department);
            
            // Create a single department-level notification (no employee lookup needed)
            AppNotification notification = AppNotification.builder()
                    .recipientEmployeeId(null) // No specific employee
                    .recipientDepartment(department) // Set department for filtering
                    .title(title)
                    .body(message)
                    .type(type)
                    .refType(type) // Use the type parameter dynamically
                    .refId(refId)
                    .channel("WEB")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            notificationRepository.save(notification);
            log.info("🔥 [DEPARTMENT NOTIFICATION] Saved department notification to database");
            
            // 🔥 NEW: Send Firebase notifications to all department employees
            for (Employee emp : departmentEmployees) {
                log.info("🔥 [DEPARTMENT NOTIFICATION] Sending Firebase to employee: {} (ID: {})", emp.getFullName(), emp.getId());
                notifyEmployeeWeb(emp.getId(), title, message, type, type, refId, null);
                notifyEmployeeMobile(emp.getId(), title, message, type, type, refId, null);
            }
            
            // 🔥 NEW: Send ONLY to department-specific topic (NO DUPLICATES)
            messagingTemplate.convertAndSend("/topic/notifications/department/" + department, Map.of(
                "title", title,
                "body", message,
                "type", type,
                "refId", refId,
                "department", department,
                "timestamp", LocalDateTime.now().toString()
            ));
            
            log.info("✅ [DEPARTMENT NOTIFICATION] Department notification sent: {} - {} department ({} employees notified via Firebase)", 
                title, department, departmentEmployees.size());
            
        } catch (Exception e) {
            log.error("❌ [DEPARTMENT NOTIFICATION] Error in sendDepartmentNotification", e);
        }
    }

    /**
     * ✅ NEW: Send notification to a specific user (by userId)
     * Used by LeadClosureApprovalService to notify requester on approve/reject
     */
    @Transactional
    public void sendUserNotification(Long userId, String title, String message, String type, Long refId) {
        if (userId == null) {
            log.warn("🔔 sendUserNotification: userId is null, skipping");
            return;
        }
        try {
            log.info("🔔 sendUserNotification: userId={}, title={}, type={}", userId, title, type);

            // Save DB notification
            AppNotification notification = AppNotification.builder()
                    .recipientEmployeeId(userId)
                    .title(title)
                    .body(message)
                    .type(type)
                    .refType(type)
                    .refId(refId)
                    .channel("WEB")
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            // Firebase push (web + mobile)
            notifyEmployeeWeb(userId, title, message, type, type, refId, null);
            notifyEmployeeMobile(userId, title, message, type, type, refId, null);

            // WebSocket direct message
            messagingTemplate.convertAndSend("/topic/notifications/user/" + userId, Map.of(
                "title", title,
                "body", message,
                "type", type,
                "refId", refId != null ? refId : 0L,
                "timestamp", LocalDateTime.now().toString()
            ));

            log.info("✅ sendUserNotification sent to userId={}", userId);

        } catch (Exception e) {
            log.error("❌ sendUserNotification failed for userId={}: {}", userId, e.getMessage(), e);
        }
    }
}
