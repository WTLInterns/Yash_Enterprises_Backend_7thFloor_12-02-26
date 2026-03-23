package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long>, JpaSpecificationExecutor<Bank> {
    
    Optional<Bank> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    // 🔥 ENHANCED: Find bank by name + branch combination (more specific)
    Optional<Bank> findByNameIgnoreCaseAndBranchNameIgnoreCase(String name, String branchName);
    
    // 🔥 FUTURE-PROOF: Find bank by name + branch + taluka (most specific)
    @Query("SELECT b FROM Bank b WHERE LOWER(b.name) = LOWER(:name) " +
           "AND (LOWER(b.branchName) = LOWER(:branch) OR (b.branchName IS NULL AND :branch IS NULL)) " +
           "AND (LOWER(b.taluka) = LOWER(:taluka) OR (b.taluka IS NULL AND :taluka IS NULL))")
    Optional<Bank> findByNameIgnoreCaseAndBranchNameIgnoreCaseAndTalukaIgnoreCase(
        @Param("name") String name, 
        @Param("branch") String branchName, 
        @Param("taluka") String taluka
    );
    
    default Bank findByIdSafe(Long id) {
        return findById(id).orElseThrow(() -> new RuntimeException("Bank not found: " + id));
    }
}
