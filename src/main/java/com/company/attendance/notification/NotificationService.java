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
    @Transactional
    public void sendRoleBasedNotification(String role, String title, String message, String type, Long refId) {
        try {
            log.info("🔥 [FIREBASE] sendRoleBasedNotification called:");
            log.info("🔥 [FIREBASE] - Role: {}", role);
            log.info("🔥 [FIREBASE] - Title: {}", title);
            log.info("🔥 [FIREBASE] - Message: {}", message);
            log.info("🔥 [FIREBASE] - Type: {}", type);
            log.info("🔥 [FIREBASE] - RefId: {}", refId);
            log.info("🔥 [FIREBASE] - Firebase Enabled: {}", firebaseEnabled);

            if ("MANAGER".equals(role)) {
                // Find all manager employees
                List<Employee> managerEmployees = employeeRepository.findByRole_Name("MANAGER");
                log.info("🔥 [FIREBASE] Found {} manager employees", managerEmployees.size());
                
                for (Employee manager : managerEmployees) {
                    log.info("🔥 [FIREBASE] Sending to manager: {} (ID: {})", manager.getFullName(), manager.getId());
                    
                    // Create notification for each manager
                    AppNotification notification = new AppNotification();
                    notification.setRecipientEmployeeId(manager.getId());
                    notification.setRecipientRole(role);
                    notification.setTitle(title);
                    notification.setBody(message);
                    notification.setType(type);
                    notification.setRefType(type);
                    notification.setRefId(refId);
                    notification.setChannel("WEB");
                    notification.setCreatedAt(LocalDateTime.now());
                    
                    notificationRepository.save(notification);
                    
                    // Send Firebase notification
                    notifyEmployeeWeb(manager.getId(), title, message, type, type, refId, null);
                    notifyEmployeeMobile(manager.getId(), title, message, type, type, refId, null);
                }
                
                // Send WebSocket notification
                messagingTemplate.convertAndSend("/topic/notifications/role/MANAGER", Map.of(
                    "title", title,
                    "body", message,
                    "type", type,
                    "refId", refId,
                    "role", role,
                    "timestamp", LocalDateTime.now().toString()
                ));
                
                log.info("✅ [FIREBASE] Manager notification sent: {} - {} managers notified", title, managerEmployees.size());
                
            } else {
                log.warn("🔥 [FIREBASE] Unsupported role for notification: {}", role);
            }
            
        } catch (Exception e) {
            log.error("❌ [FIREBASE] Error in sendRoleBasedNotification", e);
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
}
