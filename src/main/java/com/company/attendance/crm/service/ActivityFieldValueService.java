package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.*;
import com.company.attendance.crm.enums.ActivityType;
import com.company.attendance.crm.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityFieldValueService {
    private final ActivityRepository activityRepository;
    private final ActivityFieldDefinitionRepository defRepo;
    private final ActivityFieldValueRepository valRepo;

    public ActivityFieldValueService(ActivityRepository activityRepository, ActivityFieldDefinitionRepository defRepo, ActivityFieldValueRepository valRepo) {
        this.activityRepository = activityRepository;
        this.defRepo = defRepo;
        this.valRepo = valRepo;
    }

    public List<ActivityFieldValue> list(Long activityId){
        Activity act = activityRepository.findById(activityId).orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        return valRepo.findByActivity(act);
    }

    public ActivityFieldValue upsert(Long activityId, ActivityType type, String fieldKey, String value){
        Activity act = activityRepository.findById(activityId).orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        ActivityFieldDefinition def = defRepo.findByFieldKeyAndActivityType(fieldKey, type)
                .orElseThrow(() -> new IllegalArgumentException("Field not found for type"));
        ActivityFieldValue existing = valRepo.findByActivityAndFieldDefinition(act, def).orElse(null);
        if (existing == null){
            existing = new ActivityFieldValue();
            existing.setActivity(act);
            existing.setFieldDefinition(def);
        }
        existing.setValue(value);
        return valRepo.save(existing);
    }
}
