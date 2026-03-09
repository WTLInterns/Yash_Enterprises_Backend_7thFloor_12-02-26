package com.company.attendance.notification;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/notifications")
@RequiredArgsConstructor
@Slf4j
public class TestNotificationController {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;

    /**
     * Test Firebase tokens - Check all employees and their FCM tokens
     */
    @GetMapping("/firebase-tokens")
    public ResponseEntity<Map<String, Object>> getFirebaseTokens() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Employee> allEmployees = employeeRepository.findAll();
            
            Map<String, Integer> tokenStats = new HashMap<>();
            Map<String, Object> employeeDetails = new HashMap<>();
            
            int totalEmployees = 0;
            int withMobileToken = 0;
            int withWebToken = 0;
            int withAnyToken = 0;
            
            for (Employee emp : allEmployees) {
                totalEmployees++;
                Map<String, String> empTokens = new HashMap<>();
                
                String mobileToken = emp.getFcmTokenMobile();
                String webToken = emp.getFcmTokenWeb();
                
                empTokens.put("mobileToken", mobileToken != null ? mobileToken.substring(0, Math.min(20, mobileToken.length())) + "..." : "NULL");
                empTokens.put("webToken", webToken != null ? webToken.substring(0, Math.min(20, webToken.length())) + "..." : "NULL");
                
                boolean hasMobile = mobileToken != null && !mobileToken.trim().isEmpty();
                boolean hasWeb = webToken != null && !webToken.trim().isEmpty();
                
                if (hasMobile) withMobileToken++;
                if (hasWeb) withWebToken++;
                if (hasMobile || hasWeb) withAnyToken++;
                
                employeeDetails.put(emp.getFirstName() + " " + emp.getLastName() + " (ID: " + emp.getId() + ")", empTokens);
            }
            
            tokenStats.put("totalEmployees", totalEmployees);
            tokenStats.put("withMobileToken", withMobileToken);
            tokenStats.put("withWebToken", withWebToken);
            tokenStats.put("withAnyToken", withAnyToken);
            tokenStats.put("withoutAnyToken", totalEmployees - withAnyToken);
            
            response.put("stats", tokenStats);
            response.put("employees", employeeDetails);
            response.put("success", true);
            
            log.info("Firebase token check completed: {} total, {} with tokens", totalEmployees, withAnyToken);
            
        } catch (Exception e) {
            log.error("Error checking Firebase tokens: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test WebSocket notification - Send test notification to specific role/department
     */
    @PostMapping("/send-websocket")
    public ResponseEntity<Map<String, String>> sendTestWebSocket(
            @RequestParam String type,
            @RequestParam String target,
            @RequestParam(defaultValue = "Test Notification") String title,
            @RequestParam(defaultValue = "This is a test notification") String message) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            if ("role".equalsIgnoreCase(type)) {
                notificationService.sendRoleBasedNotification(target, title, message, "TEST", null);
                response.put("message", "Test role notification sent to: " + target);
                log.info("Test role notification sent to: {}", target);
                
            } else if ("department".equalsIgnoreCase(type)) {
                notificationService.sendDepartmentNotification(target, title, message, "TEST", null);
                response.put("message", "Test department notification sent to: " + target);
                log.info("Test department notification sent to: {}", target);
                
            } else {
                response.put("error", "Invalid type. Use 'role' or 'department'");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", "true");
            
        } catch (Exception e) {
            log.error("Error sending test WebSocket notification: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            response.put("success", "false");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test Firebase notification - Send test push notification
     */
    @PostMapping("/send-firebase")
    public ResponseEntity<Map<String, String>> sendTestFirebase(
            @RequestParam Long employeeId,
            @RequestParam(defaultValue = "Test Push") String title,
            @RequestParam(defaultValue = "This is a test push notification") String message) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee == null) {
                response.put("error", "Employee not found with ID: " + employeeId);
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, String> data = Map.of(
                "type", "TEST",
                "employeeId", String.valueOf(employeeId),
                "timestamp", String.valueOf(System.currentTimeMillis())
            );
            
            // Send both mobile and web notifications
            notificationService.notifyEmployeeMobile(employeeId, title, message, "TEST", "TEST", null, data);
            notificationService.notifyEmployeeWeb(employeeId, title, message, "TEST", "TEST", null, data);
            
            response.put("message", "Test Firebase notification sent to: " + employee.getFirstName() + " " + employee.getLastName());
            response.put("employeeId", String.valueOf(employeeId));
            response.put("success", "true");
            
            log.info("Test Firebase notification sent to employee {}: {}", employeeId, employee.getFirstName() + " " + employee.getLastName());
            
        } catch (Exception e) {
            log.error("Error sending test Firebase notification: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            response.put("success", "false");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test all notification types - Comprehensive test
     */
    @PostMapping("/send-all")
    public ResponseEntity<Map<String, String>> sendAllTestNotifications() {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 1. Test WebSocket notifications
            notificationService.sendRoleBasedNotification("ADMIN", "Test Admin", "Admin test notification", "TEST", null);
            notificationService.sendRoleBasedNotification("MANAGER", "Test Manager", "Manager test notification", "TEST", null);
            notificationService.sendDepartmentNotification("ACCOUNT", "Test Account", "Account department test notification", "TEST", null);
            
            // 2. Test Firebase to first available employee
            Employee firstEmployee = employeeRepository.findAll().stream().findFirst().orElse(null);
            if (firstEmployee != null) {
                Map<String, String> data = Map.of("type", "TEST_ALL", "timestamp", String.valueOf(System.currentTimeMillis()));
                notificationService.notifyEmployeeMobile(firstEmployee.getId(), "Test Mobile", "Mobile test notification", "TEST", "TEST", null, data);
                notificationService.notifyEmployeeWeb(firstEmployee.getId(), "Test Web", "Web test notification", "TEST", "TEST", null, data);
            }
            
            response.put("message", "All test notifications sent successfully");
            response.put("success", "true");
            
            log.info("All test notifications sent successfully");
            
        } catch (Exception e) {
            log.error("Error sending all test notifications: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            response.put("success", "false");
        }
        
        return ResponseEntity.ok(response);
    }
}
