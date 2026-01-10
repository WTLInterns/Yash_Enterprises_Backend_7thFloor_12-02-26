package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Activity;
import com.company.attendance.crm.entity.ActivityFieldDefinition;
import com.company.attendance.crm.entity.ActivityFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityFieldValueRepository extends JpaRepository<ActivityFieldValue, UUID> {
    List<ActivityFieldValue> findByActivity(Activity activity);
    Optional<ActivityFieldValue> findByActivityAndFieldDefinition(Activity activity, ActivityFieldDefinition def);
}
