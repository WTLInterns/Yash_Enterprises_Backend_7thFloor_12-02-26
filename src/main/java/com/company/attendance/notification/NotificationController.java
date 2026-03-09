package com.company.attendance.notification;

import com.company.attendance.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final AppNotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeFcmTokenRepository fcmTokenRepository;
    private final NotificationDiagnosticService diagnosticService;

    @PostMapping("/token")
    public ResponseEntity<?> updateToken(@Valid @RequestBody TokenRequest req) {
        log.info("Received FCM token request: employeeId={}, platform={}, token={}", 
                req.employeeId, req.platform, req.token != null ? req.token.substring(0, Math.min(20, req.token.length())) + "..." : "null");
        
        if (req.employeeId == null || req.platform == null || req.token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "employeeId, platform, token are required"));
        }

        final var empOpt = employeeRepository.findById(req.employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Employee not found"));
        }

        final var emp = empOpt.get();
        final String platform = req.platform.trim().toUpperCase();
        
        // NEW: Use multi-token system instead of single token
        try {
            log.info("Attempting to save FCM token for employeeId={}, platform={}", req.employeeId, platform);
            
            // Check if token already exists for this employee
            Optional<EmployeeFcmToken> existingToken = fcmTokenRepository.findByEmployeeIdAndToken(req.employeeId, req.token);
            if (existingToken.isPresent()) {
                log.info("Token already exists for employeeId={}, platform={}, tokenId={}", req.employeeId, platform, existingToken.get().getId());
                return ResponseEntity.ok(Map.of("message", "Token already registered"));
            }

            // Save new token
            EmployeeFcmToken newToken = EmployeeFcmToken.builder()
                    .employeeId(req.employeeId)
                    .token(req.token)
                    .platform(platform)
                    .deviceId(req.deviceId) // Optional device ID for better management
                    .build();

            log.info("Saving new FCM token: employeeId={}, platform={}, tokenLength={}", 
                    req.employeeId, platform, req.token != null ? req.token.length() : 0);
            
            EmployeeFcmToken saved = fcmTokenRepository.save(newToken);
            log.info("FCM token saved successfully: tokenId={}", saved.getId());

            // Also update legacy field for backward compatibility
            if ("MOBILE".equals(platform)) {
                emp.setFcmTokenMobile(req.token);
            } else if ("WEB".equals(platform)) {
                emp.setFcmTokenWeb(req.token);
            }
            employeeRepository.save(emp);
            log.info("Legacy FCM field updated for employeeId={}", req.employeeId);

            return ResponseEntity.ok(Map.of("message", "Token registered successfully"));

        } catch (Exception e) {
            log.error("Failed to register FCM token for employeeId={}, platform={}: {}", req.employeeId, platform, e.getMessage(), e);
            
            // FALLBACK: Use legacy single-token system if multi-token fails
            try {
                log.info("Falling back to legacy token system for employeeId={}", req.employeeId);
                if ("MOBILE".equals(platform)) {
                    emp.setFcmTokenMobile(req.token);
                } else if ("WEB".equals(platform)) {
                    emp.setFcmTokenWeb(req.token);
                }
                employeeRepository.save(emp);
                log.info("Fallback token registration successful for employeeId={}", req.employeeId);
                return ResponseEntity.ok(Map.of("message", "Token registered successfully (fallback mode)"));
            } catch (Exception fallbackEx) {
                log.error("Fallback token registration also failed: {}", fallbackEx.getMessage(), fallbackEx);
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to register token: " + e.getMessage()));
            }
        }
    }

    @GetMapping("/tokens")
    public ResponseEntity<?> getAllTokens() {
        var employees = employeeRepository.findAll();
        var tokens = employees.stream()
                .filter(emp -> emp.getFcmTokenMobile() != null || emp.getFcmTokenWeb() != null)
                .map(emp -> Map.of(
                        "employeeId", emp.getId(),
                        "firstName", emp.getFirstName() != null ? emp.getFirstName() : "",
                        "lastName", emp.getLastName() != null ? emp.getLastName() : "",
                        "email", emp.getEmail(),
                        "fcmTokenMobile", emp.getFcmTokenMobile() != null ? emp.getFcmTokenMobile().substring(0, Math.min(20, emp.getFcmTokenMobile().length())) + "..." : null,
                        "fcmTokenWeb", emp.getFcmTokenWeb() != null ? emp.getFcmTokenWeb().substring(0, Math.min(20, emp.getFcmTokenWeb().length())) + "..." : null,
                        "hasMobileToken", emp.getFcmTokenMobile() != null,
                        "hasWebToken", emp.getFcmTokenWeb() != null
                ))
                .toList();
        
        log.info("Retrieved {} employees with FCM tokens", tokens.size());
        return ResponseEntity.ok(Map.of("tokens", tokens));
    }

    @GetMapping
    public ResponseEntity<Page<AppNotification>> list(@RequestParam Long employeeId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(
                notificationRepository.findByRecipientEmployeeIdOrderByCreatedAtDesc(employeeId, PageRequest.of(page, size))
        );
    }

    @GetMapping("/by-role")
    public ResponseEntity<Page<AppNotification>> listByRole(
            @RequestParam String role,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        
        // 🔥 NEW: For ADMIN/MANAGER roles, fetch all notifications (role + department + direct)
        if ("ADMIN".equals(role) || "MANAGER".equals(role)) {
            if (employeeId == null) {
                log.warn("Admin/Manager role request missing employeeId, using role-only query");
                return ResponseEntity.ok(
                    notificationRepository.findByRecipientRoleOrderByCreatedAtDesc(role, PageRequest.of(page, size))
                );
            }
            
            log.info("Admin/Manager fetching comprehensive notifications: role={}, employeeId={}", role, employeeId);
            return ResponseEntity.ok(
                notificationRepository.findAdminNotifications(role, employeeId, PageRequest.of(page, size))
            );
        }
        
        // For other roles, use original role-only query
        return ResponseEntity.ok(
                notificationRepository.findByRecipientRoleOrderByCreatedAtDesc(role, PageRequest.of(page, size))
        );
    }

    @GetMapping("/by-department")
    public ResponseEntity<Page<AppNotification>> listByDepartment(@RequestParam String department,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(
                notificationRepository.findByRecipientDepartmentOrderByCreatedAtDesc(department, PageRequest.of(page, size))
        );
    }

    @DeleteMapping("/by-role")
    public ResponseEntity<?> deleteAllByRole(@RequestParam String role) {
        try {
            notificationRepository.deleteAllByRecipientRole(role);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            log.error("Failed to delete all notifications for role: {}", role, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete all notifications"));
        }
    }

    @DeleteMapping("/by-department")
    public ResponseEntity<?> deleteAllByDepartment(@RequestParam String department) {
        try {
            notificationRepository.deleteAllByRecipientDepartment(department);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            log.error("Failed to delete all notifications for department: {}", department, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete all notifications"));
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@RequestParam Long employeeId) {
        try {
            notificationService.markAllAsRead(employeeId);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for employeeId: {}", employeeId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to mark all notifications as read"));
        }
    }

    @PutMapping("/mark-all-read/by-role")
    public ResponseEntity<?> markAllAsReadByRole(@RequestParam String role) {
        try {
            notificationRepository.markAllAsReadByRole(role, java.time.LocalDateTime.now());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for role: {}", role, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to mark all notifications as read"));
        }
    }

    @PutMapping("/mark-all-read/by-department")
    public ResponseEntity<?> markAllAsReadByDepartment(@RequestParam String department) {
        try {
            notificationRepository.markAllAsReadByDepartment(department, java.time.LocalDateTime.now());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for department: {}", department, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to mark all notifications as read"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Failed to delete notification with id: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete notification"));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllNotifications(@RequestParam Long employeeId) {
        try {
            notificationService.deleteAllNotifications(employeeId);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Failed to delete all notifications for employeeId: {}", employeeId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete all notifications"));
        }
    }

    @GetMapping("/diagnostic/tokens")
    public ResponseEntity<?> diagnoseTokens() {
        diagnosticService.diagnoseAdminTokens();
        return ResponseEntity.ok(Map.of("message", "Diagnostic logged to console"));
    }

    @GetMapping("/diagnostic/readiness")
    public ResponseEntity<?> getReadinessReport() {
        String report = diagnosticService.getNotificationReadinessReport();
        return ResponseEntity.ok(Map.of("report", report));
    }

    @PostMapping("/diagnostic/test")
    public ResponseEntity<?> sendTestNotification() {
        diagnosticService.sendTestNotificationToAdmins();
        return ResponseEntity.ok(Map.of("message", "Test notification sent to admins"));
    }

    @GetMapping("/debug/user/{userId}")
    public ResponseEntity<?> debugUserNotifications(@PathVariable Long userId) {
        try {
            log.info("Debug: Checking notifications for userId: {}", userId);
            
            // Get notifications for this specific user
            var page = notificationRepository.findByRecipientEmployeeIdOrderByCreatedAtDesc(
                userId, org.springframework.data.domain.PageRequest.of(0, 5));
            
            // Check if user exists and is admin
            var userOpt = employeeRepository.findById(userId);
            boolean isAdmin = false;
            String userName = "Unknown";
            boolean hasMobileToken = false;
            boolean hasWebToken = false;
            
            if (userOpt.isPresent()) {
                var emp = userOpt.get();
                isAdmin = emp.getRole() != null && "ADMIN".equalsIgnoreCase(emp.getRole().getName());
                userName = emp.getFirstName() + " " + emp.getLastName();
                hasMobileToken = emp.getFcmTokenMobile() != null;
                hasWebToken = emp.getFcmTokenWeb() != null;
            }
            
            var result = Map.of(
                "userId", userId,
                "userExists", userOpt.isPresent(),
                "isAdmin", isAdmin,
                "userName", userName,
                "hasMobileToken", hasMobileToken,
                "hasWebToken", hasWebToken,
                "notificationCount", page.getTotalElements(),
                "recentNotifications", page.getContent().stream().map(n -> Map.of(
                    "id", n.getId(),
                    "title", n.getTitle(),
                    "body", n.getBody(),
                    "type", n.getType(),
                    "channel", n.getChannel(),
                    "createdAt", n.getCreatedAt(),
                    "readAt", n.getReadAt()
                )).toList()
            );
            
            log.info("Debug: Result for user {}: {} notifications, admin: {}", userId, page.getTotalElements(), isAdmin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Debug error for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test/admin-notification")
    public ResponseEntity<?> testAdminNotification() {
        try {
            notificationService.sendRoleBasedNotification(
                "ADMIN",
                "Test Admin Notification",
                "This is a role-based test notification",
                "TEST",
                999L
            );
            return ResponseEntity.ok(Map.of("message", "Admin test notification sent"));
        } catch (Exception e) {
            log.error("Failed to send admin test notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test/account-notification")
    public ResponseEntity<?> testAccountNotification() {
        try {
            notificationService.sendDepartmentNotification(
                "ACCOUNT",
                "Test Account Notification",
                "This is a department-based test notification",
                "TEST",
                998L
            );
            return ResponseEntity.ok(Map.of("message", "Account test notification sent"));
        } catch (Exception e) {
            log.error("Failed to send account test notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Data
    public static class TokenRequest {
        private Long employeeId;
        private String platform; // MOBILE / WEB
        private String token;
        private String deviceId; // Optional device identifier for better management
    } 

    @PostMapping("/test/firebase")
public ResponseEntity<?> testFirebase(@RequestParam Long employeeId) {

    notificationService.notifyEmployeeWeb(
            employeeId,
            "Firebase Test",
            "This notification is coming from Firebase",
            "TEST",
            "TEST",
            1L,
            null
    );

    return ResponseEntity.ok(Map.of("message","Firebase notification sent"));
}
}
