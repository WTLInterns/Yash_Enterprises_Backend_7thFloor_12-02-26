package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long>, JpaSpecificationExecutor<Bank> {

    // 🔥 FIX: Return List to prevent NonUniqueResultException when duplicates exist
    List<Bank> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // 🔥 FIX: Return List to prevent NonUniqueResultException
    List<Bank> findByNameIgnoreCaseAndBranchNameIgnoreCase(String name, String branchName);

    default Bank findByIdSafe(Long id) {
        return findById(id).orElseThrow(() -> new RuntimeException("Bank not found: " + id));
    }
}
