package com.company.attendance.controller;

import com.company.attendance.dto.EmployeeTrackingDto;
import com.company.attendance.entity.EmployeeTracking;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.EmployeePunch;
import com.company.attendance.entity.Task;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.EmployeeTrackingRepository;
import com.company.attendance.repository.EmployeePunchRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.service.EmployeeTrackingService;
import com.company.attendance.service.IdleDetectionService;
import com.company.attendance.service.LocationBasedAttendanceService;
import com.company.attendance.service.DecisionService;
import com.company.attendance.util.DistanceCalculator;
import com.company.attendance.util.LocationUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee-locations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmployeeLocationController {

    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final EmployeeTrackingRepository trackingRepo;
    private final EmployeeTrackingService employeeTrackingService;
    private final TaskRepository taskRepository;
    private final EmployeePunchRepository employeePunchRepository;
    private final IdleDetectionService idleDetectionService;
    private final LocationBasedAttendanceService locationBasedAttendanceService;
    private final DecisionService decisionService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<?> getAllEmployeeLocations() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // Load all employees with relationships; live status is derived from tracking table
            List<Employee> allEmployees = employeeRepository.findAllWithRelationships();

            List<Map<String, Object>> employees = allEmployees.stream()
                    .map(emp -> {
                        List<EmployeeTracking> latestTwo = trackingRepo
                                .findTop2ByEmployee_IdOrderByTimestampDesc(emp.getId());

                        EmployeeTracking latest = latestTwo.isEmpty() ? null : latestTwo.get(0);
                        EmployeeTracking previous = latestTwo.size() > 1 ? latestTwo.get(1) : null;

                        String status;
                        Double lat = null;
                        Double lng = null;
                        LocalDateTime lastUpdate = null;
                        String address = null;

                        if (latest == null) {
                            status = "OFFLINE";
                        } else {
                            lastUpdate = latest.getTimestamp();
                            address = latest.getLocationAddress();

                            long minutesSinceLast = Duration.between(lastUpdate, now).toMinutes();
                            if (minutesSinceLast > 20) {
                                // No update for more than 20 minutes -> OFFLINE, do not expose stale coordinates
                                status = "OFFLINE";
                            } else {
                                // We have a recent update, check movement to classify IDLE vs ONLINE
                                if (previous != null) {
                                    double distMeters = distanceMeters(
                                            latest.getLatitude(), latest.getLongitude(),
                                            previous.getLatitude(), previous.getLongitude());
                                    long idleMinutes = Duration.between(previous.getTimestamp(), latest.getTimestamp()).toMinutes();

                                    if (idleMinutes >= 20 && distMeters < 20.0) {
                                        status = "IDLE";
                                    } else {
                                        status = "ONLINE";
                                    }
                                } else {
                                    status = "ONLINE";
                                }

                                // For ONLINE / IDLE we expose current coordinates
                                lat = latest.getLatitude();
                                lng = latest.getLongitude();
                            }
                        }

                        Map<String, Object> empData = new HashMap<>();
                        empData.put("id", emp.getId());
                        empData.put("name", (emp.getFirstName() + " " + emp.getLastName()).trim());
                        empData.put("email", emp.getEmail());
                        empData.put("phone", emp.getPhone());
                        empData.put("role", emp.getRole() != null ? emp.getRole().getName() : null);
                        if (lat != null && lng != null) {
                            empData.put("position", Map.of("lat", lat, "lng", lng));
                        } else {
                            empData.put("position", null);
                        }
                        empData.put("status", status);
                        empData.put("lastUpdate", lastUpdate);
                        empData.put("speed", null);
                        empData.put("heading", null);
                        empData.put("address", address);
                        return empData;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "employees", employees,
                "total", employees.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching employee locations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch locations"));
        }
    }

    @PostMapping("/{employeeId}/location")
    public ResponseEntity<?> updateEmployeeLocation(
            @PathVariable Long employeeId,
            @RequestBody LocationUpdateRequest request,
            HttpServletRequest http) {
        try {
            // Verify employee exists
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            String ip = http.getRemoteAddr();
            String userAgent = http.getHeader("User-Agent");

            String address = request.getAddress();

            log.info("======================================");
            log.info("LOCATION UPDATE RECEIVED");
            log.info("Employee ID  : {}", employeeId);
            log.info("Name         : {} {}", employee.getFirstName(), employee.getLastName());
            log.info("Role         : {}", employee.getRole() != null ? employee.getRole().getName() : null);
            log.info("Latitude     : {}", request.getLatitude());
            log.info("Longitude    : {}", request.getLongitude());
            log.info("Accuracy     : {}", request.getAccuracy());
            log.info("Address      : {}", address);
            log.info("DeviceInfo   : {}", request.getDeviceInfo());
            log.info("User-Agent   : {}", userAgent);
            log.info("IP Address   : {}", ip);
            log.info("Status       : {}", request.getStatus());
            log.info("Time         : {}", LocalDateTime.now());
            log.info("======================================");

            try {
                EmployeeTrackingDto trackingDto = EmployeeTrackingDto.builder()
                        .employeeId(employeeId)
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .accuracy(request.getAccuracy())
                        .timestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now())
                        .trackingType(request.getTrackingType() != null ? request.getTrackingType() : "MOVEMENT")
                        .locationAddress(address)
                        .resolvedAddress(address)
                        .deviceInfo(request.getDeviceInfo())
                        .ipAddress(ip)
                        .isActive(true)
                        .build();
                EmployeeTracking saved = employeeTrackingService.saveTracking(trackingDto);

                List<EmployeePunch> activePunches = employeePunchRepository.findActivePunchesByEmployeeId(employeeId);
                EmployeePunch activePunch = activePunches.isEmpty() ? null : activePunches.get(0);

                // Auto Punch-In Logic - Check if employee is at customer location
                try {
                    locationBasedAttendanceService.checkAutoPunch(employeeId);
                } catch (Exception e) {
                    log.warn("Auto punch-in check failed for employee {}: {}", employeeId, e.getMessage());
                }

                // WebSocket push
                Map<String, Object> liveDto = new HashMap<>();
                liveDto.put("id", employeeId);
                liveDto.put("name", (employee.getFirstName() + " " + employee.getLastName()).trim());
                liveDto.put("role", employee.getRole() != null ? employee.getRole().getName() : null);
                liveDto.put("lat", saved.getLatitude());
                liveDto.put("lng", saved.getLongitude());
                liveDto.put("address", saved.getLocationAddress());
                liveDto.put(
                        "currentAddress",
                        saved.getResolvedAddress() != null && !saved.getResolvedAddress().isBlank()
                                ? saved.getResolvedAddress()
                                : saved.getLocationAddress()
                );
                liveDto.put("timestamp", saved.getTimestamp());
                liveDto.put("status", "ONLINE");
                liveDto.put("isPunchedIn", activePunch != null);
                liveDto.put("punchType", activePunch != null ? activePunch.getPunchType() : null);
                messagingTemplate.convertAndSend("/topic/live-locations", liveDto);
                messagingTemplate.convertAndSend("/topic/live-employees", liveDto);
            } catch (Exception e) {
                log.warn("Failed to save tracking history for employee {}: {}", employeeId, e.getMessage());
            }

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error updating location for employee {}: {}", employeeId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update location"));
        }
    }

    @GetMapping("/{employeeId}/route")
    public ResponseEntity<?> getEmployeeRoute(@PathVariable Long employeeId,
                                             @RequestParam String date) {
        try {
            LocalDate day = LocalDate.parse(date);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();

            List<EmployeeTracking> points = trackingRepo
                    .findByEmployeeIdAndTimestampBetweenAsc(employeeId, start, end);

            List<Map<String, Object>> route = points.stream()
                    .map(et -> {
                        Map<String, Object> p = new HashMap<>();
                        p.put("lat", et.getLatitude());
                        p.put("lng", et.getLongitude());
                        p.put("timestamp", et.getTimestamp());
                        p.put("address", et.getLocationAddress());
                        return p;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(route);
        } catch (Exception e) {
            log.error("Error fetching route for employee {} on {}: {}", employeeId, date, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch route"));
        }
    }

    @GetMapping("/{employeeId}/location")
    public ResponseEntity<?> getEmployeeLocation(@PathVariable Long employeeId) {
        try {
            List<EmployeeTracking> latest = trackingRepo.findLatestForEmployee(employeeId);
            if (latest.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "employeeId", employeeId,
                        "status", "not_tracked"
                ));
            }

            EmployeeTracking et = latest.get(0);
            return ResponseEntity.ok(Map.of(
                    "employeeId", employeeId,
                    "position", Map.of(
                            "lat", et.getLatitude(),
                            "lng", et.getLongitude()
                    ),
                    "status", et.getIsActive() != null && et.getIsActive() ? "active" : "offline",
                    "lastUpdate", et.getTimestamp(),
                    "address", et.getLocationAddress()
            ));
        } catch (Exception e) {
            log.error("Error fetching latest location for employee {}: {}", employeeId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch latest location"));
        }
    }

    @GetMapping("/{employeeId}/history")
    public ResponseEntity<?> getEmployeeLocationHistory(
            @PathVariable Long employeeId,
            @RequestParam String date
    ) {
        try {
            LocalDate day = LocalDate.parse(date);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atTime(LocalTime.MIDNIGHT);

            List<EmployeeTracking> history = trackingRepo
                    .findByEmployeeIdAndTimestampBetweenAsc(employeeId, start, end);

            List<Map<String, Object>> points = history.stream()
                    .map(et -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("lat", et.getLatitude());
                        point.put("lng", et.getLongitude());
                        point.put("timestamp", et.getTimestamp());
                        point.put("locationAddress", et.getLocationAddress());
                        point.put("trackingType", et.getTrackingType());
                        return point;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(points);
        } catch (Exception e) {
            log.error("Error fetching location history for employee {}: {}", employeeId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch history"));
        }
    }

    @GetMapping("/live-employees")
    public ResponseEntity<Map<String, Object>> getLiveEmployees() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            List<Map<String, Object>> employeesData = employees.stream()
                    .map(emp -> {
                        List<EmployeeTracking> latestList = trackingRepo.findLatestForEmployee(emp.getId());
                        EmployeeTracking latest = latestList.isEmpty() ? null : latestList.get(0);
                        
                        // Get active punch for employee
                        List<EmployeePunch> activePunches = employeePunchRepository.findActivePunchesByEmployeeId(emp.getId());
                        EmployeePunch activePunch = activePunches.isEmpty() ? null : activePunches.get(0);
                        
                        // Get active task
                        Task activeTask = null;
                        if (activePunch != null && activePunch.getTask() != null) {
                            activeTask = activePunch.getTask();
                        }
                        
                        Map<String, Object> empData = new HashMap<>();
                        empData.put("id", emp.getId());
                        empData.put("name", (emp.getFirstName() + " " + emp.getLastName()).trim());
                        empData.put("email", emp.getEmail());
                        empData.put("phone", emp.getPhone());
                        empData.put("role", emp.getRole() != null ? emp.getRole().getName() : null);
                        
                        if (latest != null) {
                            // Flat fields for frontend compatibility
                            empData.put("lat", latest.getLatitude());
                            empData.put("lng", latest.getLongitude());
                            empData.put("latitude", latest.getLatitude());
                            empData.put("longitude", latest.getLongitude());
                            empData.put("position", Map.of("lat", latest.getLatitude(), "lng", latest.getLongitude()));
                            empData.put("status", "ONLINE");
                            empData.put("lastUpdate", latest.getTimestamp());
                            empData.put("address", latest.getLocationAddress());
                            empData.put("currentAddress", (latest.getResolvedAddress() != null && !latest.getResolvedAddress().isBlank()) ? latest.getResolvedAddress() : latest.getLocationAddress());
                        } else {
                            empData.put("lat", null);
                            empData.put("lng", null);
                            empData.put("latitude", null);
                            empData.put("longitude", null);
                            empData.put("position", null);
                            empData.put("status", "OFFLINE");
                            empData.put("lastUpdate", null);
                            empData.put("address", null);
                            empData.put("currentAddress", null);
                        }
                        
                        // Location-based attendance information
                        empData.put("isPunchedIn", activePunch != null);
                        empData.put("punchInTime", activePunch != null ? activePunch.getPunchInTime() : null);
                        empData.put("lateMark", activePunch != null ? activePunch.getLateMark() : false);
                        empData.put("autoPunch", activePunch != null ? activePunch.getAutoPunch() : false);
                        empData.put("punchType", activePunch != null ? activePunch.getPunchType() : null);
                        
                        // CHANGE #3: SINGLE SOURCE OF TRUTH - DELEGATE TO DECISION SERVICE
                        Map<String, Object> decision = decisionService.makeEmployeeDecision(emp.getId());
                        empData.put("decision", decision);
                        
                        if (activeTask != null) {
                            // CHANGE #2: TASK ACCESS VISIBILITY - DELEGATE TO DECISION SERVICE
                            Map<String, Object> taskDecision = decisionService.makeTaskDecision(emp.getId(), activeTask.getId());
                            
                            // Get customer address for display
                            String customerAddressDisplay = null;
                            if (activeTask.getCustomerAddressId() != null) {
                                CustomerAddress custAddr = locationBasedAttendanceService.getCustomerAddressByTaskId(activeTask.getId());
                                if (custAddr != null) {
                                    customerAddressDisplay = custAddr.getAddressLine() + ", " + custAddr.getCity();
                                }
                            }
                            
                            empData.put("activeTask", Map.of(
                                "id", activeTask.getId(),
                                "name", activeTask.getTaskName(),
                                "status", activeTask.getStatus(),
                                "clientName", null, // Client removed from decision flow
                                "address", customerAddressDisplay,
                                "taskAccess", taskDecision
                            ));
                            
                            empData.put("distanceToCustomer", decision.get("distanceToCustomer"));
                            empData.put("canOperate", taskDecision.get("canUpdate"));
                        } else {
                            empData.put("activeTask", null);
                            empData.put("distanceToCustomer", decision.get("distanceToCustomer"));
                            empData.put("canOperate", false);
                        }
                        
                        return empData;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("employees", employeesData));
        } catch (Exception e) {
            log.error("Error fetching live employees: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch employees"));
        }
    }

    /**
     * Calculate distance between employee location and customer address
     * Uses customer_addresses table ONLY - no geocoding, no client.address
     */
    private double calculateDistance(double employeeLat, double employeeLng, Long taskId) {
        try {
            CustomerAddress customerAddress = locationBasedAttendanceService.getCustomerAddressByTaskId(taskId);
            if (customerAddress == null || customerAddress.getLatitude() == null || customerAddress.getLongitude() == null) {
                log.warn("Customer address or coordinates missing for task {}", taskId);
                return Double.MAX_VALUE; // FAIL CLOSED - Block operation
            }
            
            // REAL Haversine calculation using customer_addresses coordinates
            return DistanceCalculator.distanceMeters(
                employeeLat, employeeLng,
                customerAddress.getLatitude(), customerAddress.getLongitude()
            );
        } catch (Exception e) {
            log.error("Failed to calculate distance for task {}: {}", taskId, e.getMessage());
            return Double.MAX_VALUE; // FAIL CLOSED - Block operation on any error
        }
    }

    @GetMapping("/{employeeId}/tasks")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeTasks(@PathVariable Long employeeId) {
        try {
            // Get all tasks assigned to the employee
            List<Task> tasks = taskRepository.findByAssignedToEmployeeId(employeeId);
            
            List<Map<String, Object>> taskData = new ArrayList<>();
            for (Task task : tasks) {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("name", task.getTaskName());
                taskMap.put("description", task.getTaskDescription());
                taskMap.put("status", task.getStatus());
                taskMap.put("startDate", task.getStartDate());
                taskMap.put("endDate", task.getEndDate());
                taskMap.put("scheduledStartTime", task.getScheduledStartTime());
                taskMap.put("scheduledEndTime", task.getScheduledEndTime());
                taskMap.put("address", task.getAddress());
                
                // Client information removed from decision flow
                    taskMap.put("client", null);
                
                taskData.add(taskMap);
            }
            
            return ResponseEntity.ok(taskData);
        } catch (Exception e) {
            log.error("Error fetching tasks for employee {}: {}", employeeId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{employeeId}/events")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeEvents(
            @PathVariable Long employeeId,
            @RequestParam String date) {
        try {
            LocalDate queryDate = LocalDate.parse(date);
            LocalDateTime startOfDay = queryDate.atStartOfDay();
            LocalDateTime endOfDay = queryDate.atTime(LocalTime.MAX);
            
            // Get tracking records for the day
            List<EmployeeTracking> tracks = trackingRepo.findByEmployeeIdAndTimestampBetweenAsc(
                employeeId, startOfDay, endOfDay);
            
            // Detect stops and idle periods
            List<Map<String, Object>> events = detectStopsAndIdle(tracks);
            
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching events: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private List<Map<String, Object>> detectStopsAndIdle(List<EmployeeTracking> tracks) {
        List<Map<String, Object>> events = new java.util.ArrayList<>();
        
        if (tracks.size() < 2) return events;
        
        for (int i = 0; i < tracks.size() - 1; i++) {
            EmployeeTracking current = tracks.get(i);
            EmployeeTracking next = tracks.get(i + 1);
            
            long durationMinutes = Duration.between(current.getTimestamp(), next.getTimestamp()).toMinutes();
            double distance = calculateTrackingDistance(
                current.getLatitude(), current.getLongitude(),
                next.getLatitude(), next.getLongitude()
            );
            
            if (distance < 20) { // Less than 20m movement
                if (durationMinutes >= 2 && durationMinutes <= 9) {
                    // STOP event
                    Map<String, Object> event = new HashMap<>();
                    event.put("type", "STOP");
                    event.put("latitude", current.getLatitude());
                    event.put("longitude", current.getLongitude());
                    event.put("address", current.getLocationAddress());
                    event.put("startTime", current.getTimestamp());
                    event.put("endTime", next.getTimestamp());
                    event.put("durationMinutes", durationMinutes);
                    events.add(event);
                } else if (durationMinutes >= 10) {
                    // IDLE event
                    Map<String, Object> event = new HashMap<>();
                    event.put("type", "IDLE");
                    event.put("latitude", current.getLatitude());
                    event.put("longitude", current.getLongitude());
                    event.put("address", current.getLocationAddress());
                    event.put("startTime", current.getTimestamp());
                    event.put("endTime", next.getTimestamp());
                    event.put("durationMinutes", durationMinutes);
                    events.add(event);
                }
            }
        }
        
        return events;
    }

    /**
     * Calculate distance between two tracking points for stop/idle detection
     * This is for tracking-to-tracking distance, NOT customer distance
     */
    private double calculateTrackingDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // Distance in meters
    }

    @PostMapping("/idle-alert")
    public ResponseEntity<?> sendIdleAlert(@RequestBody IdleAlertRequest request) {
        try {
            // Create notification for admins
            String notificationTitle = "Employee Idle Alert";
            String notificationMessage = String.format(
                "%s has been idle for %s at location: %.6f, %.6f",
                request.getEmployeeName(),
                request.getIdleDuration(),
                request.getLatitude(),
                request.getLongitude()
            );

            // Send to all admin users
            List<Employee> admins = employeeRepository.findByRole_NameIgnoreCase("ADMIN");
            for (Employee admin : admins) {
                try {
                    // Send mobile notification
                    notificationService.notifyEmployeeMobile(
                        admin.getId(),
                        notificationTitle,
                        notificationMessage,
                        "EMPLOYEE_IDLE",
                        "LOCATION",
                        request.getEmployeeId(),
                        Map.of(
                            "employeeId", request.getEmployeeId().toString(),
                            "employeeName", request.getEmployeeName(),
                            "latitude", request.getLatitude().toString(),
                            "longitude", request.getLongitude().toString(),
                            "idleDuration", request.getIdleDuration(),
                            "timestamp", request.getTimestamp()
                        )
                    );
                    
                    // Send web notification
                    notificationService.notifyEmployeeWeb(
                        admin.getId(),
                        notificationTitle,
                        notificationMessage,
                        "EMPLOYEE_IDLE",
                        "LOCATION",
                        request.getEmployeeId(),
                        Map.of(
                            "employeeId", request.getEmployeeId().toString(),
                            "employeeName", request.getEmployeeName(),
                            "latitude", request.getLatitude().toString(),
                            "longitude", request.getLongitude().toString(),
                            "idleDuration", request.getIdleDuration(),
                            "timestamp", request.getTimestamp()
                        )
                    );
                } catch (Exception e) {
                    log.warn("Failed to send idle notification to admin {}: {}", admin.getId(), e.getMessage());
                }
            }

            log.info("Idle alert sent for employee {} at location: {}, {}", 
                request.getEmployeeId(), request.getLatitude(), request.getLongitude());

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error sending idle alert: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send idle alert"));
        }
    }

    @Data
    public static class LocationUpdateRequest {
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private String status; // active, idle, offline
        private Double speed; // km/h
        private Double heading; // degrees
        private Integer tasksCompleted;
        private Integer totalTasks;
        private String address;
        private String deviceInfo;
        private String trackingType;
        private LocalDateTime timestamp;
    }

    @Data
    public static class IdleAlertRequest {
        private Long employeeId;
        private String employeeName;
        private Double latitude;
        private Double longitude;
        private String idleDuration;
        private String timestamp;
    }

    private static double distanceMeters(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.NaN;
        }
        double R = 6371000.0; // meters
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
}
