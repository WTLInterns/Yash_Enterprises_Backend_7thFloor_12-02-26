package com.company.attendance.dto;

import com.company.attendance.enums.TaskStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {

    private Long id;

    private String taskName;
    private String taskDescription;

    private String customTaskType;

    private Long assignedToEmployeeId;
    private String assignedToEmployeeName;

    private Long createdByEmployeeId;
    private String createdByEmployeeName;

    // Date fields
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;

    private Boolean repeatTask;

    private TaskStatus status;
    private String completion;

    private String taskAgainst; // CLIENT or ROUTE (only one)
    private Long clientId;
    private String clientName;
    private Long routeId;
    private String address;

    private Long customerAddressId;

    private String internalTaskId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TaskCustomFieldValueDto> customFieldValues;
}

