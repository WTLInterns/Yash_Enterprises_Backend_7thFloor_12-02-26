package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Activity;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.enums.ActivityStatus;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.ActivityRepository;
import com.company.attendance.crm.repository.CallActivityRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.EventActivityRepository;
import com.company.attendance.crm.repository.TaskActivityRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
public class ActivityService {
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final EventActivityRepository eventActivityRepository;
    private final CallActivityRepository callActivityRepository;

    public ActivityService(
        DealRepository dealRepository,
        ActivityRepository activityRepository,
        TaskActivityRepository taskActivityRepository,
        EventActivityRepository eventActivityRepository,
        CallActivityRepository callActivityRepository
    ) {
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
        this.taskActivityRepository = taskActivityRepository;
        this.eventActivityRepository = eventActivityRepository;
        this.callActivityRepository = callActivityRepository;
    }

    public Page<Activity> list(Long dealId, ActivityType type, Pageable pageable){
        Deal deal = dealRepository.findByIdSafe(dealId);
        if (type == null) {
            return activityRepository.findByDeal(deal, pageable);
        }
        java.util.List<Activity> all = activityRepository.findByDealAndTypeOrderByCreatedAtDesc(deal, type);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        java.util.List<Activity> slice = start >= end ? java.util.List.of() : all.subList(start, end);
        return new PageImpl<>(slice, pageable, all.size());
    }

    public Activity create(Long dealId, Activity activity, Long userId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        activity.setDeal(deal);
        activity.setCreatedBy(userId);

        if (activity.getType() == ActivityType.TASK && activity.getDueDate() != null) {
            OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
            LocalDate dateUtc = activity.getDueDate().atZoneSameInstant(ZoneOffset.UTC).toLocalDate();
            LocalTime timeUtc = nowUtc.toLocalTime();
            activity.setDueDate(dateUtc.atTime(timeUtc).atOffset(ZoneOffset.UTC));
        }

        return activityRepository.save(activity);
    }

    public Activity update(Long dealId, Long activityId, Activity incoming){
        Deal deal = dealRepository.findByIdSafe(dealId);
        Activity db = activityRepository.findById(activityId).orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        if (db.getDeal() == null || db.getDeal().getId() == null || !db.getDeal().getId().equals(dealId)) {
            throw new IllegalArgumentException("Activity not in deal");
        }
        db.setDeal(deal);
        db.setName(incoming.getName());
        db.setDescription(incoming.getDescription());
        db.setOwnerId(incoming.getOwnerId());

        if (db.getType() == ActivityType.TASK && incoming.getDueDate() != null) {
            OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
            LocalDate dateUtc = incoming.getDueDate().atZoneSameInstant(ZoneOffset.UTC).toLocalDate();
            LocalTime timeUtc = nowUtc.toLocalTime();
            db.setDueDate(dateUtc.atTime(timeUtc).atOffset(ZoneOffset.UTC));
        } else {
            db.setDueDate(incoming.getDueDate());
        }

        db.setStartDate(incoming.getStartDate());
        db.setEndDate(incoming.getEndDate());
        db.setPriority(incoming.getPriority());
        db.setRepeatRule(incoming.getRepeatRule());
        db.setReminder(incoming.getReminder());
        if (incoming.getType() != null) db.setType(incoming.getType());
        db.setModifiedBy(incoming.getModifiedBy());
        return activityRepository.save(db);
    }

    public Activity patchStatus(Long dealId, Long activityId, ActivityStatus status){
        Deal deal = dealRepository.findByIdSafe(dealId);
        Activity db = activityRepository.findById(activityId).orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        if (db.getDeal() == null || db.getDeal().getId() == null || !db.getDeal().getId().equals(dealId)) {
            throw new IllegalArgumentException("Activity not in deal");
        }
        db.setDeal(deal);
        db.setStatus(status);
        return activityRepository.save(db);
    }

    public void delete(Long dealId, Long activityId){
        dealRepository.findByIdSafe(dealId);
        Activity db = activityRepository.findById(activityId).orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        if (db.getDeal() == null || db.getDeal().getId() == null || !db.getDeal().getId().equals(dealId)) {
            throw new IllegalArgumentException("Activity not in deal");
        }

        // Child tables use @MapsId (PK=FK to activities.id). Delete child first to avoid FK constraint violations.
        // Safe to call even if no child row exists.
        try { taskActivityRepository.deleteById(activityId); } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {}
        try { eventActivityRepository.deleteById(activityId); } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {}
        try { callActivityRepository.deleteById(activityId); } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {}

        activityRepository.delete(db);
    }
}
