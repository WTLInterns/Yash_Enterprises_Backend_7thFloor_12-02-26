package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.EventActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventActivityRepository extends JpaRepository<EventActivity, UUID> {
}
