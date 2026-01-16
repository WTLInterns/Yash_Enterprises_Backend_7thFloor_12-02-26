package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class EventActivityService {
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;
    private final EventActivityRepository eventRepo;

    public EventActivityService(DealRepository dealRepository, ActivityRepository activityRepository, EventActivityRepository eventRepo) {
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
        this.eventRepo = eventRepo;
    }

    public EventActivity create(Long dealId, EventActivity ev, Integer userId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        Activity base = new Activity();
        base.setDeal(deal);
        base.setType(ActivityType.EVENT);
        base.setOwnerId(userId);
        base.setName(ev != null && ev.getTitle() != null ? ev.getTitle() : "Event");

        if (ev != null && ev.getStartDateTime() != null) {
            base.setStartDate(ev.getStartDateTime().atOffset(ZoneOffset.UTC));
        }
        if (ev != null && ev.getEndDateTime() != null) {
            base.setEndDate(ev.getEndDateTime().atOffset(ZoneOffset.UTC));
        }
        if (ev != null && (ev.getLocation() != null || ev.getParticipants() != null)) {
            String loc = ev.getLocation() != null ? ev.getLocation() : "";
            String part = ev.getParticipants() != null ? ev.getParticipants() : "";
            String desc = ("Location: " + loc + (part.isBlank() ? "" : "\nParticipants: " + part)).trim();
            if (!desc.isBlank()) base.setDescription(desc);
        }
        base = activityRepository.save(base);
        ev.setActivity(base);
        // IMPORTANT: with @MapsId we should not set the child id manually; let JPA derive it from Activity
        ev.setId(null);
        return eventRepo.save(ev);
    }
}
