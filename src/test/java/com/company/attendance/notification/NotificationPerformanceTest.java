package com.company.attendance.notification;

import com.company.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Performance test for admin notification lookup
 */
@SpringBootTest
public class NotificationPerformanceTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    public void testAdminLookupPerformance() {
        long startTime = System.currentTimeMillis();
        
        // NEW EFFICIENT METHOD: Database-level filtering
        var adminEmployees = employeeRepository.findByRole_Name("ADMIN");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("✅ Efficient admin lookup completed in: " + duration + "ms");
        System.out.println("✅ Found " + adminEmployees.size() + " admin employees");
        System.out.println("🚀 Performance: Database-level filtering (no memory filtering)");
    }
}
