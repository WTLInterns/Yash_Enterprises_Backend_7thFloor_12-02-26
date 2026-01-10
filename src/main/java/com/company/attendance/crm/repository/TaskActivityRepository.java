package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {
}
