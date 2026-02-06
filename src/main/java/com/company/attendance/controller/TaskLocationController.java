package com.company.attendance.controller;

import com.company.attendance.service.LocationBasedAttendanceService;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TaskLocationController {

    private final LocationBasedAttendanceService locationBasedAttendanceService;

    /**
     * Task Update Validation - HARD BLOCK when >200m
     * PUT /api/tasks/{taskId}/status
     */
    @PutMapping("/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-Employee-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Employee-Longitude", required = false) Double longitude) {
        
        try {
            // Step 1: Validate location headers
            if (latitude == null || longitude == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "MISSING_LOCATION",
                    "message", "Location headers (X-Employee-Latitude, X-Employee-Longitude) are required",
                    "timestamp", LocalDateTime.now().toString()
                ));
            }
            
            // Step 2: Get task details for validation
            String status = (String) request.get("status");
            Integer employeeId = (Integer) request.get("employeeId");
            
            if (status == null || employeeId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "VALIDATION_ERROR",
                    "message", "Status and employeeId are required",
                    "timestamp", LocalDateTime.now().toString()
                ));
            }
            
            // Step 3: HARD LOCATION VALIDATION
            if (!locationBasedAttendanceService.validateTaskLocation(taskId, latitude, longitude)) {
                // Calculate actual distance for error response
                double distance;
                try {
                    distance = calculateDistanceToCustomer(taskId, latitude, longitude);
                } catch (Exception distanceError) {
                    log.error("Distance calculation failed for task {}: {}", taskId, distanceError.getMessage());
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "LOCATION_VALIDATION_FAILED",
                        "message", "Unable to validate location for task update",
                        "details", Map.of(
                            "taskId", taskId,
                            "reason", "Distance calculation failed"
                        ),
                        "timestamp", LocalDateTime.now().toString()
                    ));
                }
                
                return ResponseEntity.status(403).body(Map.of(
                    "error", "LOCATION_RESTRICTION",
                    "message", "Task update blocked - You are outside the 200m customer area",
                    "details", Map.of(
                        "taskId", taskId,
                        "currentDistance", Math.round(distance),
                        "maxAllowedDistance", 200.0,
                        "employeeLocation", Map.of(
                            "latitude", latitude,
                            "longitude", longitude
                        )
                    ),
                    "timestamp", LocalDateTime.now().toString()
                ));
            }
            
            // Step 4: Process task status update (if location valid)
            log.info("Task {} status updated to {} at location ({}, {})", taskId, status, latitude, longitude);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Task status updated successfully",
                "taskId", taskId,
                "status", status,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Error updating task status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Failed to update task status",
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * Task Completion Flow
     * POST /api/tasks/{taskId}/complete
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> request,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        
        try {
            // Validate distance ≤ 200m
            if (!locationBasedAttendanceService.validateTaskLocation(taskId, latitude, longitude)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Not at customer location",
                    "message", "You must be within 200 meters of customer location to complete this task"
                ));
            }
            
            // Capture live photo
            String photoUrl = (String) request.get("photoUrl");
            if (photoUrl == null || photoUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Photo required",
                    "message", "Live photo is mandatory for task completion"
                ));
            }
            
            // Save: photo URL, work_lat / work_lng, mark task COMPLETED
            log.info("Task {} completed with photo at location ({}, {})", taskId, latitude, longitude);
            
            // Punch-out task would happen here
            
            // Trigger: Customer feedback, Notifications
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Task completed successfully",
                "photoUrl", photoUrl,
                "workLocation", Map.of(
                    "latitude", latitude,
                    "longitude", longitude
                )
            ));
            
        } catch (Exception e) {
            log.error("Error completing task: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to complete task"));
        }
    }

    /**
     * Validate if employee can perform task operations
     */
    @GetMapping("/{taskId}/can-operate")
    public ResponseEntity<?> canPerformTaskOperations(
            @PathVariable Long taskId,
            @RequestParam Long employeeId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        
        try {
            // Check if punched in
            boolean hasActivePunch = locationBasedAttendanceService.hasActivePunch(employeeId);
            
            if (!hasActivePunch) {
                return ResponseEntity.ok(Map.of(
                    "canOperate", false,
                    "reason", "NOT_PUNCHED_IN",
                    "message", "Please reach customer location to punch in first"
                ));
            }
            
            // Check location
            boolean validLocation = locationBasedAttendanceService.validateTaskLocation(taskId, latitude, longitude);
            
            if (!validLocation) {
                return ResponseEntity.ok(Map.of(
                    "canOperate", false,
                    "reason", "OUTSIDE_LOCATION",
                    "message", "You must be within 200 meters of customer location"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "canOperate", true,
                "reason", "VALID",
                "message", "You can perform task operations"
            ));
            
        } catch (Exception e) {
            log.error("Error validating task operations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Validation failed"));
        }
    }
    
    /**
     * Get distance to customer location
     */
    @GetMapping("/{taskId}/distance")
    public ResponseEntity<?> getDistanceToCustomer(
            @PathVariable Long taskId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        
        try {
            // This would need implementation to get customer location and calculate distance
            // For now, return placeholder
            return ResponseEntity.ok(Map.of(
                "distance", 0,
                "withinRadius", false,
                "message", "Distance calculation not yet implemented"
            ));
            
        } catch (Exception e) {
            log.error("Error calculating distance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Distance calculation failed"));
        }
    }
    
    /**
     * Helper method to calculate actual distance to customer
     */
    private double calculateDistanceToCustomer(Long taskId, double employeeLat, double employeeLng) {
        try {
            // Get customer address via task.customer_address_id -> customer_addresses table
            // This MUST use customer_addresses table, NOT clients.latitude fallback
            CustomerAddress customerAddress = locationBasedAttendanceService.getCustomerAddressByTaskId(taskId);
            
            if (customerAddress == null) {
                throw new IllegalStateException("Customer address not found for task: " + taskId);
            }
            
            if (customerAddress.getLatitude() == null || customerAddress.getLongitude() == null) {
                throw new IllegalStateException("Customer coordinates missing for task: " + taskId);
            }
            
            // Calculate distance using Haversine formula
            return DistanceCalculator.distanceMeters(
                employeeLat, employeeLng,
                customerAddress.getLatitude(), customerAddress.getLongitude()
            );
            
        } catch (Exception e) {
            log.error("Distance calculation failed for task {}: {}", taskId, e.getMessage());
            // FAIL CLOSED - Block operation if distance cannot be calculated
            throw new IllegalStateException("Distance calculation failed for task: " + taskId, e);
        }
    }
}
