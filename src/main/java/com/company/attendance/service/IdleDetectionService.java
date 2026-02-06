package com.company.attendance.service;

import com.company.attendance.entity.EmployeeIdleEvent;
import com.company.attendance.entity.EmployeeTracking;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.Task;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.repository.EmployeeIdleEventRepository;
import com.company.attendance.repository.EmployeeTrackingRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

/**
 * TASK-SCOPED IDLE DETECTION (CRITICAL)
 * 
 * Idle popup only triggers when:
 * - task.status = IN_PROGRESS
 * - task_id IS NOT NULL
 * - Employee has active punch
 * 
 * NO idle popup while travelling
 * NO idle popup between tasks
 * ONLY during active task execution
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdleDetectionService {

    private final EmployeeTrackingRepository trackingRepo;
    private final EmployeeIdleEventRepository idleRepo;
    private final EmployeeRepository employeeRepo;
    private final EmployeePunchRepository employeePunchRepository;
    private final TaskRepository taskRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final Map<Long, LocalDateTime> lastAlertTime = new HashMap<>();
    private static final long ALERT_COOLDOWN_MINUTES = 15; // Repeat every 15 minutes as per requirements

    private static double distanceMeters(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Double.NaN;
        double R = 6371000.0;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * TASK-SCOPED idle detection
     * Only triggers during active task execution
     */
    @Scheduled(fixedRate = 300000) // every 5 minutes
    @Transactional
    public void detectIdleEmployees() {
        log.info("Running TASK-SCOPED idle detection scheduler");

        List<Long> employeeIds = trackingRepo.findEmployeesWithAtLeastTwoTrackingRecords()
                    .stream()
                    .map(r -> (Long) r[0])
                    .toList();

        for (Long empId : employeeIds) {
            // STEP 1: Check if employee has active punch (task-based)
            List<EmployeePunch> activePunches = employeePunchRepository.findActivePunchesByEmployeeId(empId);
            if (activePunches.isEmpty()) {
                log.debug("No active punch for employee {} - skipping idle detection", empId);
                continue; // No active task, skip idle detection
            }

            // STEP 2: Use the most recent active punch
            EmployeePunch activePunch = activePunches.get(0);
            Task task = activePunch.getTask();
            if (task == null || !"IN_PROGRESS".equals(task.getStatus())) {
                log.debug("Task {} not IN_PROGRESS for employee {} - skipping idle detection", 
                    task != null ? task.getId() : "null", empId);
                continue; // Task not active, skip idle detection
            }

            // STEP 3: Proceed with idle detection for active task
            var tracks = trackingRepo.findTop2ByEmployee_IdOrderByTimestampDesc(empId);
            if (tracks.size() < 2) continue;

            EmployeeTracking current = tracks.get(0);
            EmployeeTracking previous = tracks.get(1);

            // Calculate movement distance
            double distance = distanceMeters(
                current.getLatitude(), current.getLongitude(),
                previous.getLatitude(), previous.getLongitude()
            );

            // Check if employee is idle (within 30 meters)
            if (distance <= 30.0) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime lastAlert = lastAlertTime.get(empId);

                // Check if 20 minutes have passed since last alert
                if (lastAlert == null || Duration.between(lastAlert, now).toMinutes() >= ALERT_COOLDOWN_MINUTES) {
                    // Create idle event (without task relationship for now)
                    EmployeeIdleEvent idleEvent = new EmployeeIdleEvent();
                    idleEvent.setEmployee(employeeRepo.findById(empId).orElse(null));
                    idleEvent.setStartTime(current.getTimestamp());
                    idleEvent.setEndTime(now);
                    idleEvent.setLatitude(current.getLatitude());
                    idleEvent.setLongitude(current.getLongitude());
                    idleEvent.setAddress(current.getLocationAddress());

                    idleRepo.save(idleEvent);
                    lastAlertTime.put(empId, now);

                    // Send task-scoped idle alert
                    sendTaskScopedIdleAlert(empId, now, current.getLocationAddress(), employeeRepo.findById(empId).orElse(null), task);
                }
            }
        }
    }

    /**
     * Get current idle state for employee
     * Returns real idle state, not hardcoded
     */
    public String getCurrentIdleState(Long employeeId) {
        try {
            // Check if there's an active idle event for this employee
            Optional<EmployeeIdleEvent> activeIdleEvent = idleRepo.findTopByEmployeeIdAndEndTimeIsNullOrderByStartTimeDesc(employeeId);
            
            if (activeIdleEvent.isPresent()) {
                EmployeeIdleEvent activeIdle = activeIdleEvent.get();
                long durationMinutes = Duration.between(activeIdle.getStartTime(), LocalDateTime.now()).toMinutes();
                
                // Determine idle state based on duration
                if (durationMinutes >= 20) {
                    return "IDLE_ONGOING";
                } else {
                    return "IDLE_STARTING";
                }
            }
            
            return "NONE";
        } catch (Exception e) {
            log.error("Error getting idle state for employee {}: {}", employeeId, e.getMessage());
            return "NONE";
        }
    }

    /**
     * TASK-SCOPED idle alert with structured context
     * Includes detailed information for admin panel
     */
    private void sendTaskScopedIdleAlert(Long empId, LocalDateTime timestamp, String address, Employee employee, Task task) {
        try {
            String employeeName = (employee.getFirstName() + " " + employee.getLastName()).trim();
            
            // CHANGE #5: IDLE NOTIFICATION CONTEXT
            // Calculate idle duration
            long idleDurationMinutes = 0;
            String idleState = "STARTED";
            
            // Check if there's a recent idle event for this employee
            List<EmployeeIdleEvent> recentIdleEvents = idleRepo.findByEmployeeIdOrderByStartTimeDesc(empId);
            if (recentIdleEvents != null && !recentIdleEvents.isEmpty()) {
                EmployeeIdleEvent lastIdle = recentIdleEvents.get(0);
                idleDurationMinutes = Duration.between(lastIdle.getStartTime(), timestamp).toMinutes();
                idleState = "CONTINUING";
            } else {
                idleDurationMinutes = 20; // First detection = 20 minutes minimum
            }
            
            String message = String.format("Employee %s is idle at %s for %d minutes during Task #%d", 
                employeeName, 
                address != null && !address.isEmpty() ? address : "Unknown Location",
                idleDurationMinutes,
                task != null ? task.getId() : 0
            );
            
            Map<String, Object> alert = new HashMap<>();
            alert.put("employeeId", empId);
            alert.put("employeeName", employeeName);
            alert.put("taskId", task != null ? task.getId() : 0);
            alert.put("taskName", task != null ? task.getTaskName() : "Unknown Task");
            alert.put("status", "IDLE");
            alert.put("timestamp", timestamp);
            alert.put("address", address);
            alert.put("message", message);
            alert.put("latitude", address != null ? "" : "");
            alert.put("longitude", address != null ? "" : "");
            // CHANGE #5: STRUCTURED IDLE EVENT
            alert.put("idleState", idleState);
            alert.put("idleDurationMinutes", idleDurationMinutes);
            alert.put("location", address != null && !address.isEmpty() ? address : "Unknown Location");
            
            messagingTemplate.convertAndSend("/topic/alerts", alert);
            log.info("TASK-SCOPED idle alert sent for employee {} at task {}: {} (Duration: {} mins, State: {})", 
                empId, task != null ? task.getId() : 0, message, idleDurationMinutes, idleState);
        } catch (Exception e) {
            log.error("Failed to send task-scoped idle alert for employee {}: {}", empId, e.getMessage());
        }
    }
}
