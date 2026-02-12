package com.company.attendance.service;

import com.company.attendance.entity.Task;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.enums.TaskStatus;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.repository.CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final NotificationService notificationService;

    private void validateCustomerAddressLink(Task task) {
        if (task.getCustomerAddressId() == null) {
            throw new RuntimeException("customerAddressId is required");
        }
        CustomerAddress addr = customerAddressRepository.findById(task.getCustomerAddressId())
                .orElseThrow(() -> new RuntimeException("CustomerAddress not found: " + task.getCustomerAddressId()));
        if (addr.getLatitude() == null || addr.getLongitude() == null) {
            throw new RuntimeException("CustomerAddress coordinates are missing for: " + task.getCustomerAddressId());
        }
    }

    public Task create(Task task) {
        validateCustomerAddressLink(task);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        if (saved.getAssignedToEmployeeId() != null) {
            // Get client name and address for notification
            String clientName = "Unknown Client";
            String clientAddress = "";
            
            if (saved.getClientId() != null) {
                Client client = clientRepository.findById(saved.getClientId()).orElse(null);
                if (client != null) {
                    clientName = client.getName();
                }
            }
            
            if (saved.getCustomerAddressId() != null) {
                CustomerAddress address = customerAddressRepository
                    .findById(saved.getCustomerAddressId())
                    .orElse(null);

                if (address != null) {
                    clientAddress = address.getAddressType() + ": " +
                            address.getAddressLine() +
                            (address.getCity() != null ? ", " + address.getCity() : "");
                }
            }
            
            String title = String.format("📋 New Task: %s", saved.getTaskName());
            String body = String.format("Assigned: %s - Customer: %s%s - Status: %s", 
                    saved.getTaskName(),
                    clientName,
                    clientAddress.isEmpty() ? "" : " - " + clientAddress,
                    saved.getStatus());
            
            Map<String, String> data = Map.of(
                    "type", "TASK_ASSIGNED",
                    "taskId", String.valueOf(saved.getId()),
                    "taskName", saved.getTaskName(),
                    "clientName", clientName,
                    "clientAddress", clientAddress,
                    "status", saved.getStatus().toString(),
                    "message", String.format(
                            "📋 New Task Assignment:\n" +
                            "├─ Task: %s\n" +
                            "├─ Customer: %s\n" +
                            "├─ Address: %s\n" +
                            "├─ Status: %s\n" +
                            "└─ Assigned: %s",
                            saved.getTaskName(),
                            clientName,
                            clientAddress.isEmpty() ? "No address specified" : clientAddress,
                            saved.getStatus(),
                            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
                    )
            );
            notificationService.notifyEmployeeMobile(saved.getAssignedToEmployeeId(), title, body, "TASK_ASSIGNED", "TASK", saved.getId(), data);
            notificationService.notifyEmployeeWeb(saved.getAssignedToEmployeeId(), title, body, "TASK_ASSIGNED", "TASK", saved.getId(), data);
        }

        return saved;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task getById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task update(Long id, Task updated) {
        Task existing = getById(id);

        Long oldAssignee = existing.getAssignedToEmployeeId();
        TaskStatus oldStatus = existing.getStatus();

        existing.setTaskName(updated.getTaskName());
        existing.setTaskDescription(updated.getTaskDescription());
        existing.setCustomTaskType(updated.getCustomTaskType());

        existing.setAssignedToEmployeeId(updated.getAssignedToEmployeeId());
        existing.setCreatedByEmployeeId(updated.getCreatedByEmployeeId());

        existing.setScheduledStartTime(updated.getScheduledStartTime());
        existing.setScheduledEndTime(updated.getScheduledEndTime());
        existing.setRepeatTask(updated.getRepeatTask());

        existing.setTaskAgainst(updated.getTaskAgainst());
        existing.setClientId(updated.getClientId());
        existing.setRouteId(updated.getRouteId());
        existing.setAddress(updated.getAddress());
        existing.setCustomerAddressId(updated.getCustomerAddressId());

        existing.setStatus(updated.getStatus());
        existing.setCompletion(updated.getCompletion());

        existing.setUpdatedAt(LocalDateTime.now());

        validateCustomerAddressLink(existing);

        // Replace dynamic values
        existing.getCustomFieldValues().clear();
        if (updated.getCustomFieldValues() != null) {
            updated.getCustomFieldValues().forEach(val -> {
                val.setTask(existing);
                existing.getCustomFieldValues().add(val);
            });
        }

        Task saved = taskRepository.save(existing);

        if (saved.getAssignedToEmployeeId() != null && (oldAssignee == null || !oldAssignee.equals(saved.getAssignedToEmployeeId()))) {
            // Get client name and address for notification
            String clientName = "Unknown Client";
            String clientAddress = "";
            
            if (saved.getClientId() != null) {
                Client client = clientRepository.findById(saved.getClientId()).orElse(null);
                if (client != null) {
                    clientName = client.getName();
                }
            }
            
            if (saved.getCustomerAddressId() != null) {
                CustomerAddress address = customerAddressRepository
                    .findById(saved.getCustomerAddressId())
                    .orElse(null);

                if (address != null) {
                    clientAddress = address.getAddressType() + ": " +
                            address.getAddressLine() +
                            (address.getCity() != null ? ", " + address.getCity() : "");
                }
            }
            
            // Create professional task reassignment notification
            String title = "📋 Task Updated: " + saved.getTaskName();
            String body = String.format("Updated: %s - Customer: %s%s - Status: %s", 
                    saved.getTaskName(),
                    clientName,
                    clientAddress.isEmpty() ? "" : " - " + clientAddress,
                    saved.getStatus());
            
            Map<String, String> data = Map.of(
                    "type", "TASK_ASSIGNED",
                    "taskId", String.valueOf(saved.getId()),
                    "taskName", saved.getTaskName(),
                    "clientName", clientName,
                    "clientAddress", clientAddress,
                    "status", saved.getStatus().toString(),
                    "message", String.format(
                            "📋 Task Updated:\n" +
                            "┌─ Task: %s\n" +
                            "├─ Customer: %s\n" +
                            "├─ Address: %s\n" +
                            "├─ Status: %s\n" +
                            "└─ Updated: %s",
                            saved.getTaskName(),
                            clientName,
                            clientAddress.isEmpty() ? "No address specified" : clientAddress,
                            saved.getStatus(),
                            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
                    )
            );
            notificationService.notifyEmployeeMobile(saved.getAssignedToEmployeeId(), title, body, "TASK_ASSIGNED", "TASK", saved.getId(), data);
            notificationService.notifyEmployeeWeb(saved.getAssignedToEmployeeId(), title, body, "TASK_ASSIGNED", "TASK", saved.getId(), data);
        }

        return saved;
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
    
    // NEW: Update task status and notify ADMIN
    public void updateTaskStatus(Long taskId, String status, Long employeeId) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

            // Validate status
            TaskStatus taskStatus;
            try {
                taskStatus = TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid task status: " + status + ". Valid statuses are: " + 
                    java.util.Arrays.toString(TaskStatus.values()));
            }

            task.setStatus(taskStatus);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            // 🔔 SEND NOTIFICATION TO ALL ADMINS (both MOBILE and WEB) - with error handling
            try {
                List<Employee> admins = employeeRepository.findByRole_NameIgnoreCase("ADMIN");
                
                if (admins.isEmpty()) {
                    log.warn("No admin users found for notification");
                    return; // Don't throw exception, just log warning
                }

                // Get employee name for notification
                Employee updatedByEmployee = employeeRepository.findById(employeeId).orElse(null);
                String employeeName = updatedByEmployee != null ? 
                        updatedByEmployee.getFirstName() + " " + updatedByEmployee.getLastName() : 
                        "Employee " + employeeId;

                // Get client name and address for notification
                String clientName = "Unknown Client";
                String clientAddress = "";
                
                if (task.getClientId() != null) {
                    Client client = clientRepository.findById(task.getClientId()).orElse(null);
                    if (client != null) {
                        clientName = client.getName();
                    }
                }
                
                if (task.getCustomerAddressId() != null) {
                    CustomerAddress address = customerAddressRepository
                        .findById(task.getCustomerAddressId())
                        .orElse(null);

                    if (address != null) {
                        clientAddress = address.getAddressType() + ": " +
                                address.getAddressLine() +
                                (address.getCity() != null ? ", " + address.getCity() : "");
                    }
                }

            // Create simple, clean notification message with employee name in title
            String title = task.getTaskName() + " - " + employeeName;
            String shortBody = String.format("Customer: %s | Status: %s", 
                    clientName, 
                    taskStatus);
            
            // Enhanced formatted message with employee name highlighted
            String fullMessage = String.format(
                    "Task Status Updated\n\n" +
                    "Task: %s\n" +
                    "Customer: %s\n" +
                    "Address: %s\n" +
                    "Status: %s\n" +
                    "Updated by: %s",
                    task.getTaskName(),
                    clientName,
                    clientAddress.isEmpty() ? "No address" : clientAddress,
                    taskStatus,
                    employeeName
            );

            Map<String, String> data = new HashMap<>();
            data.put("taskId", task.getId().toString());
            data.put("type", "TASK_STATUS_UPDATED");
            data.put("taskName", task.getTaskName());
            data.put("clientName", clientName);
            data.put("clientAddress", clientAddress);
            data.put("status", taskStatus.toString());
            data.put("updatedBy", employeeName);
            data.put("message", fullMessage);

            // Send simple notification to all admins
            for (Employee admin : admins) {
                try {
                    notificationService.notifyEmployeeMobile(
                            admin.getId(),
                            title,
                            shortBody,
                            "TASK_STATUS_UPDATED",
                            "TASK",
                            task.getId(),
                            data
                    );
                    
                    notificationService.notifyEmployeeWeb(
                            admin.getId(),
                            title,
                            shortBody,
                            "TASK_STATUS_UPDATED",
                            "TASK",
                            task.getId(),
                            data
                    );
                } catch (Exception notifyEx) {
                    log.warn("Failed to send notification to admin {}: {}", admin.getId(), notifyEx.getMessage());
                    // Continue with other admins even if one fails
                }
            }
                
                log.info("Task status updated successfully: {} -> {} by employee {}", 
                    taskId, taskStatus, employeeId);
                    
            } catch (Exception notificationEx) {
                log.error("Notification failed but task was updated: {}", notificationEx.getMessage());
                // Don't fail the whole operation if notification fails
            }
            
        } catch (RuntimeException e) {
            log.error("Failed to update task status: {}", e.getMessage());
            throw e; // Re-throw runtime exceptions
        } catch (Exception e) {
            log.error("Unexpected error updating task status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update task status: " + e.getMessage());
        }
    }
}
