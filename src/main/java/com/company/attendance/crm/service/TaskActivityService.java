package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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

    public TaskActivity create(Long dealId, TaskActivity task, Integer userId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        Activity base = new Activity();
        base.setDeal(deal);
        base.setType(ActivityType.TASK);
        base.setOwnerId(userId);

        final String taskName = (task != null && task.getTaskName() != null && !task.getTaskName().isBlank())
                ? task.getTaskName()
                : "Task";
        base.setName(taskName);

        OffsetDateTime dueDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        if (task != null && task.getDueDate() != null) {
            dueDateTime = task.getDueDate()
                .atTime(LocalTime.now(ZoneOffset.UTC))
                .atOffset(ZoneOffset.UTC);
        }
        base.setDueDate(dueDateTime);

        log.info("Creating task activity for deal {} with name '{}'", dealId, taskName);
        base = activityRepository.save(base);
        task.setActivity(base);
        // IMPORTANT: with @MapsId we should not set the child id manually; let JPA derive it from Activity
        task.setId(null);
        return taskRepo.save(task);
    }
}
