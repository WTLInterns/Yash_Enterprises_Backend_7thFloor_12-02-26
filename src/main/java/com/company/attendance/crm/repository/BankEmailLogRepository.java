package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.BankEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankEmailLogRepository extends JpaRepository<BankEmailLog, Long> {
    List<BankEmailLog> findByBankIdOrderBySentAtDesc(Long bankId);
}
