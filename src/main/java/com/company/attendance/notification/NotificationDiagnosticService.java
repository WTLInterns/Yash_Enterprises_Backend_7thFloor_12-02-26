package com.company.attendance.notification;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Utility service to ensure bidirectional notification functionality
 * Helps diagnose and fix FCM token issues for admin users
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDiagnosticService {

    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    /**
     * Diagnose FCM token issues for admin users
     */
    public void diagnoseAdminTokens() {
        List<Employee> admins = employeeRepository.findByRole_NameIgnoreCase("ADMIN");
        
        log.info("=== FCM Token Diagnostic Report ===");
        log.info("Found {} admin users", admins.size());
        
        for (Employee admin : admins) {
            log.info("Admin: {} {} (ID: {})", 
                    admin.getFirstName(), admin.getLastName(), admin.getId());
            log.info("  - Mobile Token: {}", 
                    admin.getFcmTokenMobile() != null ? "Present" : "Missing");
            log.info("  - Web Token: {}", 
                    admin.getFcmTokenWeb() != null ? "Present" : "Missing");
            
            if (admin.getFcmTokenMobile() == null && admin.getFcmTokenWeb() == null) {
                log.warn("  Admin has no FCM tokens - will not receive notifications!");
            }
        }
        log.info("=== End Diagnostic Report ===");
    }

    /**
     * Send test notification to all admins to verify connectivity
     */
    public void sendTestNotificationToAdmins() {
        List<Employee> admins = employeeRepository.findByRole_NameIgnoreCase("ADMIN");
        
        if (admins.isEmpty()) {
            log.warn("No admin users found for test notification");
            return;
        }

        String title = "Test Notification";
        String body = "This is a test to verify admin notification system is working";
        
        for (Employee admin : admins) {
            // Send to both mobile and web
            notificationService.notifyEmployeeMobile(
                    admin.getId(), title, body, "TEST", "SYSTEM", null, null);
            notificationService.notifyEmployeeWeb(
                    admin.getId(), title, body, "TEST", "SYSTEM", null, null);
        }
        
        log.info("Test notification sent to {} admin users", admins.size());
    }

    /**
     * Get summary of notification readiness
     */
    public String getNotificationReadinessReport() {
        List<Employee> admins = employeeRepository.findByRole_NameIgnoreCase("ADMIN");
        
        long adminsWithMobileTokens = admins.stream()
                .filter(admin -> admin.getFcmTokenMobile() != null)
                .count();
        
        long adminsWithWebTokens = admins.stream()
                .filter(admin -> admin.getFcmTokenWeb() != null)
                .count();
        
        long adminsWithAnyToken = admins.stream()
                .filter(admin -> admin.getFcmTokenMobile() != null || admin.getFcmTokenWeb() != null)
                .count();

        return String.format(
                "Notification Readiness:\n" +
                "Total Admins: %d\n" +
                "With Mobile Tokens: %d\n" +
                "With Web Tokens: %d\n" +
                "With Any Token: %d\n" +
                "Ready for Notifications: %s",
                admins.size(),
                adminsWithMobileTokens,
                adminsWithWebTokens,
                adminsWithAnyToken,
                adminsWithAnyToken > 0 ? "YES" : "NO"
        );
    }
}
