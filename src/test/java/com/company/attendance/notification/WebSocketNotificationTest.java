package com.company.attendance.notification;

import com.company.attendance.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test WebSocket notification functionality
 */
@SpringBootTest
public class WebSocketNotificationTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    public void testAdminWebSocketNotification() {
        // This should create notification and send WebSocket event
        notificationService.sendRoleBasedNotification(
            "ADMIN",
            "Test WebSocket Notification",
            "This is a test for WebSocket connectivity",
            "TEST_TYPE",
            123L
        );
        
        System.out.println("✅ WebSocket notification sent successfully");
        System.out.println("🔍 Check browser console for WebSocket message:");
        System.out.println("   - Should see 'Received admin notification' log");
        System.out.println("   - Should trigger refresh in Topbar");
    }
}
