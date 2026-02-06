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

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final AppNotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
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
        if ("MOBILE".equals(platform)) {
            emp.setFcmTokenMobile(req.token);
            log.info("Updated MOBILE FCM token for employeeId={}", req.employeeId);
        } else if ("WEB".equals(platform)) {
            emp.setFcmTokenWeb(req.token);
            log.info("Updated WEB FCM token for employeeId={}", req.employeeId);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "platform must be MOBILE or WEB"));
        }

        employeeRepository.save(emp);
        return ResponseEntity.ok(Map.of("ok", true));
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
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to mark all as read"));
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

    @Data
    public static class TokenRequest {
        private Long employeeId;
        private String platform; // MOBILE / WEB
        private String token;
    }
}
