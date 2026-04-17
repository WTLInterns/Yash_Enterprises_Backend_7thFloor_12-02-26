package com.company.attendance.service;

import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.entity.EmployeeTracking;
import com.company.attendance.entity.Task;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.EmployeeTrackingRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.util.DistanceCalculator;
import com.company.attendance.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationBasedAttendanceService {
    
    // ✅ SINGLE SOURCE OF TRUTH FOR GEOFENCE RADIUS
    private static final double GEOFENCE_RADIUS_METERS = 200.0; // PRODUCTION
    
    private final EmployeePunchRepository employeePunchRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final EmployeeTrackingRepository employeeTrackingRepository;
    private final NotificationService notificationService;
    private final AttendanceService attendanceService;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(10, 0);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    /**
     * LOCATION-FIRST PUNCH LOGIC (CRITICAL)
     * 
     * IF distance > 200m → NO punch (even before 10 AM)
     * IF distance ≤ 200m → punch
     *     IF time > 10:00 → late_mark = true
     * 
     * No "early punch without location"
     * No manual override unless admin
     * Location is the single source of truth
     */
    @Transactional
    public void checkAutoPunch(Long employeeId) {
        log.info("LOCATION-FIRST punch check for employee {}", employeeId);
        
        try {
            final java.time.LocalDate today = java.time.LocalDate.now(BUSINESS_ZONE);
            // Get active task for employee
            List<Task> activeTasks = taskRepository.findActiveTasksByEmployeeId(employeeId);
            Task activeTask = activeTasks.isEmpty() ? null : activeTasks.get(0);
            if (activeTask == null) {
                log.debug("No active task found for employee {}", employeeId);
                return;
            }
            
            // ✅ FIXED: Now validate task location with customer address
            if (activeTask.getCustomerAddressId() == null) {
                log.debug("Task has no customer address assigned - skipping location validation");
                return;
            }
            
            // Get customer address for geofence validation
            CustomerAddress customerAddress = activeTask.getCustomerAddress();
            if (customerAddress == null || customerAddress.getLatitude() == null || customerAddress.getLongitude() == null) {
                log.debug("Customer address coordinates not found - skipping location validation");
                return;
            }
            
            // Get latest employee location
            Optional<EmployeeTracking> latestLocationOpt = employeeTrackingRepository.findFirstByEmployee_IdOrderByTimestampDesc(employeeId);
            if (latestLocationOpt.isEmpty()) {
                log.debug("No employee location found for geofence validation");
                return;
            }
            EmployeeTracking latestLocation = latestLocationOpt.get();
            
            // Calculate distance from customer location
            double distance = calculateDistance(
                latestLocation.getLatitude(), 
                latestLocation.getLongitude(),
                customerAddress.getLatitude(), 
                customerAddress.getLongitude()
            );
            
            boolean isWithinGeofence = distance <= GEOFENCE_RADIUS_METERS;
            
            log.info("Geofence validation - Employee: {}, Distance: {}m, Within: {}", 
                employeeId, Math.round(distance), isWithinGeofence);
            
            // Update punch record with geofence info
            List<EmployeePunch> activeToday = employeePunchRepository.findActivePunchesByEmployeeIdAndDate(employeeId, today);
            Optional<EmployeePunch> activePunchOpt = (activeToday == null || activeToday.isEmpty())
                    ? Optional.empty()
                    : Optional.ofNullable(activeToday.get(0));
            
            // ✅ RESTORED: Auto punch creation when inside geofence
            if (isWithinGeofence && activePunchOpt.isEmpty()) {
                log.info("Auto punching in employee {} at customer location (distance: {}m)", employeeId, Math.round(distance));
                autoPunchIn(employeeId, activeTask.getId(), distance, 
                    latestLocation.getLatitude(), latestLocation.getLongitude());
            }
            
            if (activePunchOpt.isPresent()) {
                EmployeePunch activePunch = activePunchOpt.get();
                activePunch.setIsWithinGeofence(isWithinGeofence);
                activePunch.setDistanceFromCustomer(distance);
                employeePunchRepository.save(activePunch);
            }
            
        } catch (Exception e) {
            log.error("Error in checkAutoPunch for employee {}: {}", employeeId, e.getMessage(), e);
        }
    }
    
    /**
     * Method 2: autoPunchIn(...)
     * Creates EMPLOYEE_PUNCH record and derives attendance
     */
    @Transactional
    public void autoPunchIn(Long employeeId, Long taskId, double distance, double employeeLat, double employeeLng) {
        log.info("Auto punch-in for employee {} at task {} (distance: {}m)", employeeId, taskId, distance);
        
        // Before 10 AM → late_mark = false
        // After 10 AM → late_mark = true
        boolean isLate = LocalTime.now().isAfter(LATE_THRESHOLD);
        
        EmployeePunch punch = EmployeePunch.builder()
            .employee(employeeRepository.findById(employeeId).orElse(null))
            .task(taskRepository.findById(taskId).orElse(null))
            .punchInTime(LocalDateTime.now())
            .lateMark(isLate)
            .distanceFromCustomer(distance)
            .autoPunch(true)
            .latitude(employeeLat)
            .longitude(employeeLng)
            .createdAt(LocalDateTime.now())
            .build();
        
        punch = employeePunchRepository.save(punch);
        
        // CRITICAL: Generate attendance from punch (single source of truth)
        try {
            attendanceService.generateFromPunch(punch);
            log.info("Attendance generated from punch for employee {}", employeeId);
        } catch (Exception e) {
            log.error("Failed to generate attendance from punch: {}", e.getMessage(), e);
            // Don't fail the punch, but log the error
        }
        
        // Send notification
        sendPunchInNotification(employeeId, taskId, isLate, distance);
        
        // Broadcast attendance event to WebSocket
        broadcastAttendanceEvent("PUNCH_IN", employeeId, taskId, isLate, distance, employeeLat, employeeLng);
        
        log.info("Auto punch-in completed for employee {} - {}", employeeId, isLate ? "LATE" : "ON TIME");
    }
    
    /**
     * Validate task operations - must be within 200m
     */
    public boolean validateTaskLocation(Long taskId, double employeeLat, double employeeLng) {
        try {
            // Get task and customer address
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null || task.getCustomerAddressId() == null) {
                log.warn("Task {} has no customer address configured", taskId);
                return false; // FAIL CLOSED
            }
            
            CustomerAddress customerAddress = getCustomerAddressByTaskId(taskId);
            if (customerAddress == null || customerAddress.getLatitude() == null || customerAddress.getLongitude() == null) {
                log.warn("Customer address coordinates missing for task {}", taskId);
                return false; // FAIL CLOSED
            }
            
            // Calculate distance
            double distance = calculateDistance(employeeLat, employeeLng, 
                customerAddress.getLatitude(), customerAddress.getLongitude());
            
            boolean withinGeofence = distance <= 200; // PRODUCTION: 200m
            log.debug("Task location validation - Task: {}, Distance: {}m, Valid: {}", 
                taskId, Math.round(distance), withinGeofence);
            
            return withinGeofence;
            
        } catch (Exception e) {
            log.error("Error validating task location for task {}: {}", taskId, e.getMessage());
            return false; // FAIL CLOSED
        }
    }
    
    /**
     * Check if employee has active punch
     */
    public boolean hasActivePunch(Long employeeId) {
        final java.time.LocalDate today = java.time.LocalDate.now(BUSINESS_ZONE);
        return !employeePunchRepository.findActivePunchesByEmployeeIdAndDate(employeeId, today).isEmpty();
    }
    
    /**
     * 12 AM Midnight Auto Punch-Out (Scheduler)
     */
    @Scheduled(cron = "0 0 19 * * ?", zone = "Asia/Kolkata")
    @Transactional
    public void autoPunchOutAll() {
        log.info("Running 7 PM auto punch-out scheduler zone={}", BUSINESS_ZONE);
        
        List<EmployeePunch> activePunches = employeePunchRepository.findAllActivePunches();

        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        
        for (EmployeePunch punch : activePunches) {
            if (punch.getPunchOutTime() != null) {
                continue;
            }
            punch.setPunchOutTime(now);
            punch.setPunchTime(now);
            punch.setPunchType("OUT");
            punch.setAutoPunch(true);
            punch.setUpdatedAt(now);
            employeePunchRepository.save(punch);
            
            log.info("Auto punched out employee {} from task {}", punch.getEmployee().getId(), punch.getTask().getId());

            try {
                attendanceService.upsertFromLegacyPunchEvent(punch);
            } catch (Exception e) {
                log.warn("Auto punch-out: attendance update failed punchId={} employeeId={} taskId={}",
                        punch.getId(),
                        punch.getEmployee() != null ? punch.getEmployee().getId() : null,
                        punch.getTask() != null ? punch.getTask().getId() : null,
                        e);
            }
            
            // Send notification
            sendAutoPunchOutNotification(punch.getEmployee().getId(), punch.getTask().getId());
        }
        
        log.info("Auto punch-out completed for {} employees", activePunches.size());
    }
    
    /**
     * Get latest employee location
     */
    private double[] getLatestEmployeeLocation(Long employeeId) {
        try {
            // For now, return dummy location since findLatestByEmployeeId doesn't exist
            // TODO: Implement findLatestByEmployeeId method in EmployeeTrackingRepository
            log.debug("Latest employee location lookup bypassed - repository method needs implementation");
            return new double[]{19.0760, 72.8777}; // Mumbai coordinates as fallback
        } catch (Exception e) {
            log.warn("Failed to get latest location for employee {}: {}", employeeId, e.getMessage());
        }
        return new double[]{0, 0};
    }
    
    /**
     * Broadcast attendance event to WebSocket
     */
    private void broadcastAttendanceEvent(String type, Long employeeId, Long taskId, boolean isLate, double distance, double lat, double lng) {
        try {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee == null) return;
            
            Map<String, Object> event = new HashMap<>();
            event.put("type", type);
            event.put("employeeId", employeeId);
            event.put("employeeName", employee.getFirstName() + " " + employee.getLastName());
            event.put("taskId", taskId);
            event.put("isLate", isLate);
            event.put("distance", distance);
            event.put("latitude", lat);
            event.put("longitude", lng);
            event.put("timestamp", LocalDateTime.now());
            event.put("title", type.equals("PUNCH_IN") ? "Auto Punch-In" : "Auto Punch-Out");
            event.put("message", String.format(
                "%s punched in %s at customer location (%.0fm)",
                employee.getFirstName() + " " + employee.getLastName(),
                isLate ? "LATE" : "ON TIME",
                distance
            ));
            
            // Send to WebSocket topic
            messagingTemplate.convertAndSend("/topic/attendance-events", event);
            
            log.info("Attendance event broadcasted: {} for employee {}", type, employeeId);
            
        } catch (Exception e) {
            log.error("Failed to broadcast attendance event: {}", e.getMessage(), e);
        }
    }

    /**
     * Send punch-in notification
     */
    private void sendPunchInNotification(Long employeeId, Long taskId, boolean isLate, double distance) {
        try {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee == null) return;
            
            String title = isLate ? "Late Punch-In" : "Punch-In Successful";
            String message = String.format(
                "%s punched in %s for task %d (Distance: %.0fm)",
                employee.getFirstName() + " " + employee.getLastName(),
                isLate ? "LATE" : "ON TIME",
                taskId,
                distance
            );
            
            notificationService.notifyEmployee(
                employeeId,
                "MOBILE",
                title,
                message,
                "ATTENDANCE",
                "TASK",
                taskId,
                null
            );
            
        } catch (Exception e) {
            log.error("Failed to send punch-in notification", e);
        }
    }
    
    /**
     * Send auto punch-out notification
     */
    private void sendAutoPunchOutNotification(Long employeeId, Long taskId) {
        try {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee == null) return;
            
            String title = "Auto Punch-Out";
            String message = String.format(
                "%s auto punched out from task %d at 10:00 PM",
                employee.getFirstName() + " " + employee.getLastName(),
                taskId
            );
            
            notificationService.notifyEmployee(
                employeeId,
                "MOBILE",
                title,
                message,
                "ATTENDANCE",
                "TASK",
                taskId,
                null
            );
            
        } catch (Exception e) {
            log.error("Failed to send auto punch-out notification", e);
        }
    }
    
    /**
     * Get customer address via task.customer_address_id -> customer_addresses table
     * This method MUST use customer_addresses table, NOT clients.latitude fallback
     */
    public CustomerAddress getCustomerAddressByTaskId(Long taskId) {
        try {
            // First get task to find customer_address_id
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                log.warn("Task not found: {}", taskId);
                return null;
            }
            
            Task task = taskOpt.get();
            Long customerAddressId = task.getCustomerAddressId();
            
            if (customerAddressId == null) {
                log.warn("No customer_address_id for task: {}", taskId);
                return null;
            }
            
            // Get customer address from customer_addresses table ONLY
            return customerAddressRepository.findById(customerAddressId).orElse(null);
            
        } catch (Exception e) {
            log.error("Error getting customer address for task {}: {}", taskId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1  
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Earth's radius in meters
        final double EARTH_RADIUS = 6371000.0;
        
        // Convert to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Differences
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        
        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
