package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TaskActivityService {
    private static final Logger log = LoggerFactory.getLogger(TaskActivityService.class);
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;
    private final TaskActivityRepository taskRepo;

    public TaskActivityService(DealRepository dealRepository, ActivityRepository activityRepository, TaskActivityRepository taskRepo) {
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
        this.taskRepo = taskRepo;
    }

    public TaskActivity create(UUID dealId, TaskActivity task, UUID userId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        Activity base = new Activity();
        base.setDeal(deal);
        base.setType(ActivityType.TASK);
        base.setOwnerId(userId);

        final String taskName = (task != null && task.getTaskName() != null && !task.getTaskName().isBlank())
                ? task.getTaskName()
                : "Task";
        base.setName(taskName);

        if (task != null && task.getDueDate() != null) {
            base.setDueDate(task.getDueDate().atStartOfDay().atOffset(ZoneOffset.UTC));
        }

        log.info("Creating task activity for deal {} with name '{}'", dealId, taskName);
        base = activityRepository.save(base);
        task.setActivity(base);
        // IMPORTANT: with @MapsId we should not set the child id manually; let JPA derive it from Activity
        task.setId(null);
        return taskRepo.save(task);
    }
}
