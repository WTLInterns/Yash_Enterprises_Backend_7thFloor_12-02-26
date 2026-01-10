package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ActivityFieldDefinition;
import com.company.attendance.crm.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityFieldDefinitionRepository extends JpaRepository<ActivityFieldDefinition, UUID> {
    boolean existsByFieldKeyIgnoreCaseAndActivityType(String fieldKey, ActivityType activityType);
    Optional<ActivityFieldDefinition> findByFieldKeyAndActivityType(String fieldKey, ActivityType activityType);
    List<ActivityFieldDefinition> findByActivityTypeAndActiveTrue(ActivityType activityType);
}
