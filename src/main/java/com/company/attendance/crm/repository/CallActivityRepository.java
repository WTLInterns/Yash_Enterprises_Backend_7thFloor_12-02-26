package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.CallActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CallActivityRepository extends JpaRepository<CallActivity, UUID> {
}
