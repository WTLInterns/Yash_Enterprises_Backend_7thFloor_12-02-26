package com.company.attendance.controller;

import com.company.attendance.dto.TaskDto;
import com.company.attendance.dto.TaskCustomFieldValueDto;
import com.company.attendance.entity.Task;
import com.company.attendance.entity.TaskCustomField;
import com.company.attendance.entity.TaskCustomFieldValue;
import com.company.attendance.mapper.TaskMapper;
import com.company.attendance.service.TaskService;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.repository.TaskCustomFieldRepository;
import com.company.attendance.notification.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final TaskCustomFieldRepository taskCustomFieldRepository;
    private final NotificationService notificationService;
    private final com.company.attendance.repository.CustomerAddressRepository customerAddressRepository;

    // Resolve all lazy relations via IDs — no open session required
    private TaskDto toEnrichedDto(Task task) {
        TaskDto dto = taskMapper.toDto(task);

        if (task.getClientId() != null) {
            clientRepository.findById(task.getClientId())
                .ifPresent(c -> dto.setClientName(c.getName()));
        }

        if (task.getAssignedToEmployeeId() != null) {
            employeeRepository.findByIdWithRelationships(task.getAssignedToEmployeeId())
                .ifPresent(e -> dto.setAssignedToEmployeeName(e.getFirstName() + " " + e.getLastName()));
        }

        if (task.getCreatedByEmployeeId() != null) {
            employeeRepository.findByIdWithRelationships(task.getCreatedByEmployeeId())
                .ifPresent(e -> dto.setCreatedByEmployeeName(e.getFirstName() + " " + e.getLastName()));
        }

        if (task.getCustomerAddressId() != null) {
            customerAddressRepository.findById(task.getCustomerAddressId()).ifPresent(addr -> {
                dto.setCustomerAddressId(addr.getId());
                String addressText = addr.getAddressType() + ": ";
                if (addr.getAddressLine() != null) addressText += addr.getAddressLine();
                if (addr.getCity() != null) addressText += ", " + addr.getCity();
                if (addr.getPincode() != null) addressText += " - " + addr.getPincode();
                dto.setAddress(addressText);
            });
        }

        if (task.getCustomFieldValues() != null) {
            List<TaskCustomFieldValueDto> customFieldDtos = task.getCustomFieldValues().stream()
                    .map(value -> {
                        TaskCustomFieldValueDto valueDto = new TaskCustomFieldValueDto();
                        if (value.getField() != null) {
                            valueDto.setTaskCustomFieldId(value.getField().getId());
                            valueDto.setFieldId(value.getField().getId());
                            valueDto.setFieldKey(value.getField().getFieldKey());
                            valueDto.setFieldLabel(value.getField().getFieldLabel());
                            valueDto.setFieldType(value.getField().getFieldType());
                            valueDto.setRequired(value.getField().getRequired());
                        }
                        valueDto.setValue(value.getValue());
                        return valueDto;
                    }).toList();
            dto.setCustomFieldValues(customFieldDtos);
        }

        return dto;
    }

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<TaskDto>> listTasks(
            @RequestParam(value = "department", required = false) String departmentParam,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartment) {
        
        try {
            // 🔥 HEADER FALLBACK: Derive from employeeId if headers missing
            String derivedUserRole = userRole;
            String derivedUserDepartment = userDepartment;
            
            if (userRole == null || userDepartment == null) {
                if (userId != null) {
                    var employee = employeeRepository.findByIdWithRelationships(Long.valueOf(userId))
                        .orElse(null);
                    if (employee != null) {
                        derivedUserRole = employee.getRole() != null ? employee.getRole().getName() : null;
                        derivedUserDepartment = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
                    }
                }
                // If still no role, default to ADMIN behavior (no userId required for dashboard calls)
                if (derivedUserRole == null) {
                    derivedUserRole = "ADMIN";
                }
            }
            
            List<Task> tasks;
            
            // 🔥 DEPARTMENT-AWARE FILTERING
            switch (derivedUserRole != null ? derivedUserRole.toUpperCase() : "UNKNOWN") {
                case "ADMIN":
                case "MANAGER":
                    // ADMIN/MANAGER: Can filter by department or see all — no userId required
                    if (departmentParam != null && !departmentParam.isEmpty()) {
                        tasks = taskService.findByDepartment(departmentParam);
                    } else {
                        tasks = taskService.findAll();
                    }
                    break;
                    
                case "TL":
                    // 🔥 NEW: TL sees tasks created FOR their department (cross-department tasks)
                    // TL can create tasks for any department, but only sees tasks assigned TO their department
                    if (derivedUserDepartment != null) {
                        tasks = taskService.findByTargetDepartment(derivedUserDepartment);
                    } else {
                        tasks = List.of();
                    }
                    break;
                    
                case "EMPLOYEE":
                    // Employee sees only tasks assigned to them
                    if (userId == null) {
                        return ResponseEntity.badRequest().body(List.of());
                    }
                    tasks = taskService.getTasksForEmployee(Long.valueOf(userId));
                    break;
                    
                default:
                    tasks = List.of();
            }
            
            List<TaskDto> dtos = tasks.stream().map(task -> {
            TaskDto dto = taskMapper.toDto(task);

            if (task.getClient() != null) {
                dto.setClientName(task.getClient().getName());
            }

            if (task.getAssignedToEmployee() != null) {
                dto.setAssignedToEmployeeName(
                    task.getAssignedToEmployee().getFirstName() + " " + task.getAssignedToEmployee().getLastName()
                );
            }

            if (task.getCreatedByEmployee() != null) {
                dto.setCreatedByEmployeeName(
                    task.getCreatedByEmployee().getFirstName() + " " + task.getCreatedByEmployee().getLastName()
                );
            }

            // ✅ FIX 1: Enrich address fields
            if (task.getCustomerAddress() != null) {
                dto.setCustomerAddressId(task.getCustomerAddress().getId());
                
                String addressText = task.getCustomerAddress().getAddressType() + ": ";
                if (task.getCustomerAddress().getAddressLine() != null) {
                    addressText += task.getCustomerAddress().getAddressLine();
                }
                if (task.getCustomerAddress().getCity() != null) {
                    addressText += ", " + task.getCustomerAddress().getCity();
                }
                if (task.getCustomerAddress().getPincode() != null) {
                    addressText += " - " + task.getCustomerAddress().getPincode();
                }
                dto.setAddress(addressText);
            }

            if (task.getCustomFieldValues() != null) {
                List<TaskCustomFieldValueDto> customFieldDtos = task.getCustomFieldValues().stream()
                        .map(value -> {
                            TaskCustomFieldValueDto valueDto = new TaskCustomFieldValueDto();
                            if (value.getField() != null) {
                                valueDto.setTaskCustomFieldId(value.getField().getId());
                                valueDto.setFieldId(value.getField().getId());
                                valueDto.setFieldKey(value.getField().getFieldKey());
                                valueDto.setFieldLabel(value.getField().getFieldLabel());
                                valueDto.setFieldType(value.getField().getFieldType());
                                valueDto.setRequired(value.getField().getRequired());
                            }
                            valueDto.setValue(value.getValue());
                            return valueDto;
                        }).toList();
                dto.setCustomFieldValues(customFieldDtos);
            }

            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error fetching tasks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TaskDto>> getTasksForEmployee(@PathVariable Long employeeId) {
        List<Task> tasks = taskService.getTasksForEmployee(employeeId);
        List<TaskDto> dtos = tasks.stream().map(this::toEnrichedDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @Transactional(readOnly = true)
    @GetMapping("/client/{clientId}/employee/{employeeId}")
    public ResponseEntity<List<TaskDto>> getTasksForEmployeeAndClient(
            @PathVariable Long clientId,
            @PathVariable Long employeeId
    ) {
        List<Task> tasks = taskService.getTasksForEmployeeAndClient(clientId, employeeId);
        List<TaskDto> dtos = tasks.stream().map(this::toEnrichedDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        Task task = taskService.getById(id);
        TaskDto dto = taskMapper.toDto(task);

        // Add client name using relation
        if (task.getClient() != null) {
            dto.setClientName(task.getClient().getName());
        }

        // Add assigned employee name using relation
        if (task.getAssignedToEmployee() != null) {
            dto.setAssignedToEmployeeName(
                task.getAssignedToEmployee().getFirstName() + " " + task.getAssignedToEmployee().getLastName()
            );
        }

        // Add created by employee name using relation
        if (task.getCreatedByEmployee() != null) {
            dto.setCreatedByEmployeeName(
                task.getCreatedByEmployee().getFirstName() + " " + task.getCreatedByEmployee().getLastName()
            );
        }

        // ✅ FIX 1: Enrich address fields for single task
        if (task.getCustomerAddress() != null) {
            dto.setCustomerAddressId(task.getCustomerAddress().getId());
            
            String addressText = task.getCustomerAddress().getAddressType() + ": ";
            if (task.getCustomerAddress().getAddressLine() != null) {
                addressText += task.getCustomerAddress().getAddressLine();
            }
            if (task.getCustomerAddress().getCity() != null) {
                addressText += ", " + task.getCustomerAddress().getCity();
            }
            if (task.getCustomerAddress().getPincode() != null) {
                addressText += " - " + task.getCustomerAddress().getPincode();
            }
            dto.setAddress(addressText);
        }

        // Add custom field values
        if (task.getCustomFieldValues() != null) {
            List<TaskCustomFieldValueDto> customFieldDtos = task.getCustomFieldValues().stream()
                    .map(value -> {
                        TaskCustomFieldValueDto valueDto = new TaskCustomFieldValueDto();
                        valueDto.setTaskCustomFieldId(value.getField().getId());
                        valueDto.setFieldKey(value.getField().getFieldKey());
                        valueDto.setFieldLabel(value.getField().getFieldLabel());
                        valueDto.setValue(value.getValue());
                        return valueDto;
                    }).toList();
            dto.setCustomFieldValues(customFieldDtos);
        }

        return ResponseEntity.ok(dto);
    }

    @Transactional
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto dto) {
        try {
            Task task = taskMapper.toEntity(dto);

            // Handle custom field values
            task.getCustomFieldValues().clear();
            if (dto.getCustomFieldValues() != null) {
                dto.getCustomFieldValues().forEach(valueDto -> {
                    Long fieldId = valueDto.getTaskCustomFieldId() != null ? valueDto.getTaskCustomFieldId() : valueDto.getFieldId();
                    if (fieldId == null) {
                        throw new RuntimeException("Task custom field id is required");
                    }
                    TaskCustomField field = taskCustomFieldRepository.findById(fieldId)
                            .orElseThrow(() -> new RuntimeException("Task custom field not found: " + fieldId));

                    TaskCustomFieldValue value = new TaskCustomFieldValue();
                    value.setTask(task);
                    value.setField(field);
                    value.setValue(valueDto.getValue());
                    task.getCustomFieldValues().add(value);
                });
            }

            Task created = taskService.create(task);

            // Send notification to assigned employee
            if (created.getAssignedToEmployee() != null) {
                Map<String, String> data = new HashMap<>();
                data.put("taskId", created.getId().toString());
                data.put("taskTitle", created.getTaskName());
                data.put("action", "created");

                notificationService.notifyEmployeeMobile(
                        created.getAssignedToEmployee().getId(),
                        "New Task Assigned",
                        "You have been assigned a new task: " + created.getTaskName(),
                        "TASK_ASSIGNED",
                        "TASK",
                        created.getId(),
                        data
                );
            }

            return ResponseEntity.ok(toEnrichedDto(created));
        } catch (RuntimeException e) {
            log.warn("Task create rejected: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long id, 
            @Valid @RequestBody TaskDto dto,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartment) {
        try {
            // 🔥 HEADER FALLBACK: Derive from employeeId if headers missing
            String derivedUserRole = userRole;
            String derivedUserDepartment = userDepartment;
            
            if (userRole == null || userDepartment == null) {
                if (userId == null) {
                    return ResponseEntity.status(403).body(null);
                }
                
                var employee = employeeRepository.findByIdWithRelationships(Long.valueOf(userId))
                    .orElse(null);
                
                if (employee != null) {
                    derivedUserRole = employee.getRole() != null ? employee.getRole().getName() : null;
                    derivedUserDepartment = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
                }
            }
            
            // 🔥 DEPARTMENT AUTHORIZATION: Check if user can update this task
            Task existingTask = taskService.getById(id);

            // Use eager-loaded employee to avoid LazyInitializationException
            String taskDepartment = null;
            if (existingTask.getAssignedToEmployee() != null) {
                Long empId = existingTask.getAssignedToEmployee().getId();
                var empOpt = employeeRepository.findByIdWithRelationships(empId);
                if (empOpt.isPresent() && empOpt.get().getDepartment() != null) {
                    taskDepartment = empOpt.get().getDepartment().getName();
                }
            }

            // Also check task's own department field as fallback
            if (taskDepartment == null) {
                taskDepartment = existingTask.getDepartment();
            }

            // ADMIN and MANAGER can update any task; others must match department
            boolean isAdminOrManager = "ADMIN".equals(derivedUserRole) || "MANAGER".equals(derivedUserRole);
            if (!isAdminOrManager && taskDepartment != null && !taskDepartment.equals(derivedUserDepartment)) {
                return ResponseEntity.status(403).body(null);
            }
            
            Task task = taskMapper.toEntity(dto);

            // Build custom field values list WITHOUT touching task.getCustomFieldValues() on detached entity
            List<TaskCustomFieldValue> newFieldValues = new ArrayList<>();
            if (dto.getCustomFieldValues() != null && !dto.getCustomFieldValues().isEmpty()) {
                for (var valueDto : dto.getCustomFieldValues()) {
                    Long fieldId = valueDto.getTaskCustomFieldId() != null ? valueDto.getTaskCustomFieldId() : valueDto.getFieldId();
                    if (fieldId == null) throw new RuntimeException("Task custom field id is required");
                    TaskCustomField field = taskCustomFieldRepository.findById(fieldId)
                            .orElseThrow(() -> new RuntimeException("Task custom field not found: " + fieldId));
                    TaskCustomFieldValue value = new TaskCustomFieldValue();
                    value.setField(field);
                    value.setValue(valueDto.getValue());
                    newFieldValues.add(value);
                }
            }
            // Set the pre-built list so TaskService.update() can use it without lazy-loading
            task.getCustomFieldValues().clear();
            task.getCustomFieldValues().addAll(newFieldValues);

            Task updated = taskService.update(id, task);

            // Send notification to assigned employee about task update
            if (updated.getAssignedToEmployee() != null) {
                Map<String, String> data = new HashMap<>();
                data.put("taskId", updated.getId().toString());
                data.put("taskTitle", updated.getTaskName());
                data.put("action", "updated");
                data.put("status", updated.getStatus() != null ? updated.getStatus().toString() : "");

                notificationService.notifyEmployeeMobile(
                        updated.getAssignedToEmployee().getId(),
                        "Task Updated",
                        "Your task '" + updated.getTaskName() + "' has been updated. Status: " + updated.getStatus(),
                        "TASK_UPDATED",
                        "TASK",
                        updated.getId(),
                        data
                );
            }

            return ResponseEntity.ok(toEnrichedDto(updated));
        } catch (RuntimeException e) {
            log.warn("Task update rejected (id={}): {}", id, e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload
    ) {
        try {
            String newStatus = payload.get("status");
            String employeeIdStr = payload.get("employeeId");
            
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
            
            if (employeeIdStr == null || employeeIdStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employee ID is required"));
            }
            
            Long updatedByEmployeeId;
            try {
                updatedByEmployeeId = Long.valueOf(employeeIdStr);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid employee ID format"));
            }
            
            taskService.updateTaskStatus(id, newStatus, updatedByEmployeeId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Task status updated successfully"));
        } catch (RuntimeException e) {
            log.error("Error updating task status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating task status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test-notification")
    public ResponseEntity<?> testNotification(@RequestBody Map<String, String> payload) {
        try {
            String employeeIdStr = payload.get("employeeId");
            if (employeeIdStr == null || employeeIdStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employee ID is required"));
            }
            
            Long employeeId = Long.valueOf(employeeIdStr);
            
            // Send test notification
            notificationService.notifyEmployeeMobile(
                employeeId,
                "🔔 Test Notification",
                "This is a test notification from the system",
                "TEST",
                "SYSTEM",
                null,
                Map.of("test", "true", "timestamp", String.valueOf(System.currentTimeMillis()))
            );
            
            notificationService.notifyEmployeeWeb(
                employeeId,
                "🔔 Test Notification",
                "This is a test notification from the system",
                "TEST",
                "SYSTEM",
                null,
                Map.of("test", "true", "timestamp", String.valueOf(System.currentTimeMillis()))
            );
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Test notification sent"));
        } catch (Exception e) {
            log.error("Failed to send test notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send test notification: " + e.getMessage()));
        }
    }
}

