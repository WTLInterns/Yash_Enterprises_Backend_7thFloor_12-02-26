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

            log.info("Notification saved for employeeId={}, channel={}, type={}", employeeId, channel, type);

            Employee emp = employeeRepository.findById(employeeId).orElse(null);
            if (emp == null) return;

            String token = null;
            if ("MOBILE".equalsIgnoreCase(channel)) {
                token = emp.getFcmTokenMobile();
            } else if ("WEB".equalsIgnoreCase(channel)) {
                token = emp.getFcmTokenWeb();
            }

            if (token == null || token.isBlank()) {
                log.warn("No {} token found for employeeId={}", channel, employeeId);
                return;
            }

            if (!firebaseEnabled) {
                return;
            }

            try {
                Message.Builder builder = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder().setTitle(title).setBody(body).build());

                if (data != null) {
                    builder.putAllData(data);
                }

                FirebaseMessaging.getInstance().send(builder.build());
            } catch (Exception e) {
                log.warn("FCM send failed (employeeId={}, channel={}): {}", employeeId, channel, e.getMessage());
                // keep DB notification even if push fails
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
     * ADD THIS NEW METHOD INSIDE EXISTING CLASS
     * Send role-based notification with WebSocket support
     */
    @Transactional
    public void sendRoleBasedNotification(String role, String title, String message, String type, Long refId) {
        try {
            if ("ADMIN".equals(role)) {
                // Find all admin employees (EFFICIENT: Database-level filtering)
                List<Employee> adminEmployees = employeeRepository.findByRole_Name("ADMIN");
                
                // Create notification for each admin
                for (Employee admin : adminEmployees) {
                    AppNotification notification = new AppNotification();
                    notification.setRecipientEmployeeId(admin.getId());
                    notification.setTitle(title);
                    notification.setBody(message);
                    notification.setType(type);
                    notification.setRefType("ADDRESS_EDIT");
                    notification.setRefId(refId);
                    notification.setChannel("WEB");
                    notification.setCreatedAt(LocalDateTime.now());
                    
                    notificationRepository.save(notification);
                }
                
                // Send WebSocket to admin topic
                messagingTemplate.convertAndSend("/topic/admin-notifications", Map.of(
                    "title", title,
                    "message", message,
                    "type", type,
                    "refId", refId,
                    "role", role,
                    "timestamp", LocalDateTime.now().toString()
                ));
                
                log.info("Admin notification sent: {} - {} admins notified", title, adminEmployees.size());
                
            } else if ("EMPLOYEE".equals(role)) {
                // For employee notifications, we need specific employee ID
                // This will be handled by existing notifyEmployeeMobile method
                log.info("Employee notification requested: {} - use notifyEmployeeMobile for specific employee", title);
            }
            
        } catch (Exception e) {
            log.error("Failed to send role-based notification: {}", e.getMessage(), e);
        }
    }
}
