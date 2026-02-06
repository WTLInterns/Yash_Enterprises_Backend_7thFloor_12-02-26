package com.company.attendance.service;

import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.entity.EmployeeTracking;
import com.company.attendance.entity.Task;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.EmployeeTrackingRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DECISION SERVICE - SINGLE SOURCE OF TRUTH
 * 
 * This service makes ALL business decisions.
 * Controllers must ONLY delegate to this service.
 * Frontend must ONLY display decisions from this service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionService {

    private final LocationBasedAttendanceService locationBasedAttendanceService;
    private final EmployeeTrackingRepository trackingRepo;
    private final EmployeePunchRepository employeePunchRepository;
    private final TaskRepository taskRepository;
    private final IdleDetectionService idleDetectionService;

    /**
     * Make unified decision for employee state
     * This is the SINGLE SOURCE OF TRUTH for frontend
     */
    public Map<String, Object> makeEmployeeDecision(Long employeeId) {
        Map<String, Object> decision = new HashMap<>();
        
        try {
            // Get latest tracking
            List<EmployeeTracking> latestList = trackingRepo.findLatestForEmployee(employeeId);
            EmployeeTracking latest = latestList.isEmpty() ? null : latestList.get(0);
            
            // Get active punch
            List<EmployeePunch> activePunches = employeePunchRepository.findActivePunchesByEmployeeId(employeeId);
            EmployeePunch activePunch = activePunches.isEmpty() ? null : activePunches.get(0);
            
            // Get active task
            Task activeTask = null;
            if (activePunch != null && activePunch.getTask() != null) {
                activeTask = activePunch.getTask();
            }
            
            // Connection status
            String connectionStatus = latest != null ? "ONLINE" : "OFFLINE";
            decision.put("connectionStatus", connectionStatus);
            
            // Location and distance calculation
            String locationStatus = "GPS_OFF";
            double distanceToCustomer = Double.MAX_VALUE;
            boolean withinGeofence = false;
            boolean customerLocationConfigured = false;
            
            if (latest != null && activeTask == null) {
                // No active task -> cannot compare to customer geofence
                locationStatus = "NO_ACTIVE_TASK";
                withinGeofence = false;
                distanceToCustomer = Double.MAX_VALUE;
            } else if (latest != null && activeTask != null && activeTask.getCustomerAddressId() != null) {
                CustomerAddress customerAddress = locationBasedAttendanceService.getCustomerAddressByTaskId(activeTask.getId());
                if (customerAddress != null && customerAddress.getLatitude() != null && customerAddress.getLongitude() != null) {
                    distanceToCustomer = DistanceCalculator.distanceMeters(
                        latest.getLatitude(), latest.getLongitude(),
                        customerAddress.getLatitude(), customerAddress.getLongitude()
                    );
                    withinGeofence = distanceToCustomer <= 200;
                    locationStatus = withinGeofence ? "IN_RANGE" : "OUT_OF_RANGE";
                    customerLocationConfigured = true;
                } else {
                    // FAIL CLOSED - cannot validate geofence without coordinates
                    locationStatus = "CUSTOMER_LOCATION_MISSING";
                    withinGeofence = false;
                    distanceToCustomer = Double.MAX_VALUE;
                }
            } else if (latest != null && activeTask != null && activeTask.getCustomerAddressId() == null) {
                // FAIL CLOSED - active task exists but not linked to customer address
                locationStatus = "CUSTOMER_ADDRESS_ID_MISSING";
                withinGeofence = false;
                distanceToCustomer = Double.MAX_VALUE;
            }
            
            // Work status determination
            String workStatus = determineWorkStatus(latest, activePunch, withinGeofence);
            if (activeTask != null && !customerLocationConfigured) {
                workStatus = "LOCATION_UNAVAILABLE";
            }
            decision.put("workStatus", workStatus);
            
            // Permissions
            boolean canOperateTask = withinGeofence && activePunch != null;
            boolean canPunchIn = withinGeofence;

            final Double distanceToCustomerOut;
            if (distanceToCustomer == Double.MAX_VALUE) {
                distanceToCustomerOut = null;
            } else {
                distanceToCustomerOut = distanceToCustomer;
            }
            
            decision.put("distanceToCustomer", distanceToCustomerOut);
            decision.put("withinGeofence", withinGeofence);
            decision.put("canOperateTask", canOperateTask);
            decision.put("canPunchIn", canPunchIn);
            
            // Blocked reason
            String blockedReason = determineBlockedReason(withinGeofence, activePunch);
            decision.put("blockedReason", blockedReason);
            
            // User message
            String message;
            if (activeTask != null && !customerLocationConfigured) {
                message = "Customer location is not configured";
            } else {
                message = generateUserMessage(locationStatus, distanceToCustomerOut, activePunch);
            }
            decision.put("message", message);
            
            // Idle state from IdleDetectionService
            String idleState = idleDetectionService.getCurrentIdleState(employeeId);
            decision.put("idleState", idleState);
            
            // Log decision context
            log.info("[DECISION] employeeId={} taskId={} status={} distance={}m punch={} idleState={}", 
                employeeId, 
                activeTask != null ? activeTask.getId() : null,
                latest != null ? "GPS_ON" : "GPS_OFF",
                Math.round(distanceToCustomer),
                activePunch != null ? "ACTIVE" : "NONE",
                idleState
            );
            
        } catch (Exception e) {
            log.error("Error making decision for employee {}: {}", employeeId, e.getMessage());
            // Fail closed
            decision.put("connectionStatus", "OFFLINE");
            decision.put("workStatus", "NOT_WORKING");
            decision.put("distanceToCustomer", Double.MAX_VALUE);
            decision.put("withinGeofence", false);
            decision.put("canOperateTask", false);
            decision.put("canPunchIn", false);
            decision.put("blockedReason", "SYSTEM_ERROR");
            decision.put("message", "System error - please try again");
            decision.put("idleState", "NONE");
        }
        
        return decision;
    }
    
    /**
     * Make task access decision
     */
    public Map<String, Object> makeTaskDecision(Long employeeId, Long taskId) {
        Map<String, Object> decision = new HashMap<>();
        
        try {
            // Get task
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                decision.put("canUpdate", false);
                decision.put("blockedReason", "TASK_NOT_FOUND");
                return decision;
            }
            
            Task task = taskOpt.get();
            
            // Get employee tracking
            List<EmployeeTracking> latestList = trackingRepo.findLatestForEmployee(employeeId);
            EmployeeTracking latest = latestList.isEmpty() ? null : latestList.get(0);
            
            // Get active punch
            List<EmployeePunch> activePunches = employeePunchRepository.findActivePunchesByEmployeeId(employeeId);
            EmployeePunch activePunch = activePunches.isEmpty() ? null : activePunches.get(0);
            
            // Calculate distance
            boolean canUpdate = false;
            String blockedReason = null;

            if (latest == null) {
                canUpdate = false;
                blockedReason = "GPS_OFF";
            } else if (task.getCustomerAddressId() == null) {
                canUpdate = false;
                blockedReason = "CUSTOMER_LOCATION_MISSING";
            } else {
                CustomerAddress customerAddress = locationBasedAttendanceService.getCustomerAddressByTaskId(taskId);
                if (customerAddress != null && customerAddress.getLatitude() != null && customerAddress.getLongitude() != null) {
                    double distance = DistanceCalculator.distanceMeters(
                        latest.getLatitude(), latest.getLongitude(),
                        customerAddress.getLatitude(), customerAddress.getLongitude()
                    );
                    canUpdate = distance <= 200 && activePunch != null;
                    if (!canUpdate) {
                        if (distance > 200) {
                            blockedReason = "OUT_OF_GEOFENCE";
                        } else if (activePunch == null) {
                            blockedReason = "NOT_PUNCHED_IN";
                        }
                    }
                } else {
                    blockedReason = "CUSTOMER_LOCATION_MISSING";
                }
            }
            
            decision.put("canUpdate", canUpdate);
            decision.put("blockedReason", blockedReason);
            
        } catch (Exception e) {
            log.error("Error making task decision for employee {} task {}: {}", employeeId, taskId, e.getMessage());
            decision.put("canUpdate", false);
            decision.put("blockedReason", "SYSTEM_ERROR");
        }
        
        return decision;
    }
    
    private String determineWorkStatus(EmployeeTracking latest, EmployeePunch activePunch, boolean withinGeofence) {
        if (latest == null) {
            return "OFFLINE";
        }
        
        if (activePunch == null) {
            return "NOT_WORKING";
        }
        
        if (withinGeofence) {
            return "WORKING";
        } else {
            return "NOT_AT_CUSTOMER";
        }
    }
    
    private String determineBlockedReason(boolean withinGeofence, EmployeePunch activePunch) {
        if (!withinGeofence) {
            return "OUTSIDE_CUSTOMER_LOCATION";
        } else if (activePunch == null) {
            return "NOT_PUNCHED_IN";
        }
        return null;
    }
    
    private String generateUserMessage(String locationStatus, Double distanceToCustomer, EmployeePunch activePunch) {
        if ("OUT_OF_RANGE".equals(locationStatus)) {
            if (distanceToCustomer == null) {
                return "Location unavailable";
            }
            return String.format("You are %.1fkm away from customer location", distanceToCustomer / 1000);
        } else if ("IN_RANGE".equals(locationStatus) && activePunch == null) {
            return "You can punch in at this location";
        } else if ("IN_RANGE".equals(locationStatus)) {
            return "You are at the customer location";
        } else {
            return "Location unavailable";
        }
    }
}
