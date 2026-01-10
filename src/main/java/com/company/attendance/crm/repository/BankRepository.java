package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID>, JpaSpecificationExecutor<Bank> {
    boolean existsByBankNameIgnoreCase(String bankName);
    Optional<Bank> findByBankNameIgnoreCase(String bankName);

    Page<Bank> findByActiveTrue(Pageable pageable);
}
