package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.*;
import org.springframework.stereotype.Service;

@Service
public class CallActivityService {
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;
    private final CallActivityRepository callRepo;

    public CallActivityService(DealRepository dealRepository, ActivityRepository activityRepository, CallActivityRepository callRepo) {
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
        this.callRepo = callRepo;
    }

    public CallActivity create(Long dealId, CallActivity call, Integer userId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        Activity base = new Activity();
        base.setDeal(deal);
        base.setType(ActivityType.CALL);
        base.setOwnerId(userId);
        base.setName("Call");
        base = activityRepository.save(base);
        call.setActivity(base);
        // IMPORTANT: with @MapsId we should not set the child id manually; let JPA derive it from Activity
        call.setId(null);
        return callRepo.save(call);
    }
}
