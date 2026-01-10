package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Activity;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.enums.ActivityStatus;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.ActivityRepository;
import com.company.attendance.crm.repository.DealRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class ActivityService {
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;

    public ActivityService(DealRepository dealRepository, ActivityRepository activityRepository) {
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
    }

    public Page<Activity> list(UUID dealId, ActivityType type, Pageable pageable){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        if (type == null) {
            return activityRepository.findAll(pageable); // fallback
        }

        // Prefer a native query compatible with legacy UUID storage (BINARY(36) padded) to ensure deal_id matches.
        // We return a Page by slicing the in-memory result.
        java.util.List<Activity> all = activityRepository.findByDealIdCompatAndTypeOrderByCreatedAtDesc(dealId.toString(), type.name());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        java.util.List<Activity> slice = start >= end ? java.util.List.of() : all.subList(start, end);
        return new PageImpl<>(slice, pageable, all.size());
    }

    public Activity create(UUID dealId, Activity activity, UUID userId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        activity.setDeal(deal);
        activity.setCreatedBy(userId);
        return activityRepository.save(activity);
    }

    public Activity update(UUID dealId, UUID activityId, Activity incoming){
        if (activityRepository.countByIdCompatAndDealIdCompat(activityId.toString(), dealId.toString()) != 1) {
            throw new IllegalArgumentException("Activity not in deal");
        }

        int updated = activityRepository.updateByIdCompat(activityId.toString(),
                incoming.getName(),
                incoming.getDescription(),
                incoming.getOwnerId(),
                incoming.getDueDate(),
                incoming.getStartDate(),
                incoming.getEndDate(),
                incoming.getPriority(),
                incoming.getRepeatRule(),
                incoming.getReminder(),
                incoming.getType() != null ? incoming.getType().name() : null,
                incoming.getModifiedBy());
        if (updated != 1) throw new IllegalStateException("Failed to update activity");

        // Return updated entity (re-fetch via compat)
        return activityRepository.findByIdCompat(activityId.toString());
    }

    public Activity patchStatus(UUID dealId, UUID activityId, ActivityStatus status){
        if (activityRepository.countByIdCompatAndDealIdCompat(activityId.toString(), dealId.toString()) != 1) {
            throw new IllegalArgumentException("Activity not in deal");
        }

        // For status patch, we still need to fetch and save to only change status
        Activity db = activityRepository.findByIdCompat(activityId.toString());
        if (db == null) throw new IllegalArgumentException("Activity not found");
        db.setStatus(status);
        return activityRepository.save(db);
    }

    public void delete(UUID dealId, UUID activityId){
        if (activityRepository.countByIdCompatAndDealIdCompat(activityId.toString(), dealId.toString()) != 1) {
            throw new IllegalArgumentException("Activity not in deal");
        }

        int deleted = activityRepository.deleteByIdCompat(activityId.toString());
        if (deleted != 1) throw new IllegalStateException("Failed to delete activity");
    }
}
