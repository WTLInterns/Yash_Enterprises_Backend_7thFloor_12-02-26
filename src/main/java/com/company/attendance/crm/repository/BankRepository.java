package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Integer>, JpaSpecificationExecutor<Bank> {
    boolean existsByNameIgnoreCase(String name);
    
    default Bank findByIdSafe(Integer id) {
        return findById(id).orElseThrow(() ->
            new RuntimeException("Bank not found: " + id)
        );
    }
}
