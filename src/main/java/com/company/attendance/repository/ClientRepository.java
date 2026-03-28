package com.company.attendance.repository;

import com.company.attendance.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByEmail(String email);
    
    Optional<Client> findByName(String name);

    Optional<Client> findByCustomerNumber(String customerNumber);

    Optional<Client> findByNameAndContactName(String name, String contactName);
    
    List<Client> findByIsActive(Boolean isActive);
    
    @Query("SELECT c FROM Client c WHERE c.isActive = true OR c.isActive IS NULL")
    List<Client> findAllActive();
    
    /**
     * 🔥 DEPARTMENT-AWARE: Find clients by their IDs (for department filtering via Deal)
     */
    List<Client> findByIdInAndIsActiveTrue(List<Long> ids);
    
    @Query("SELECT COUNT(c) FROM Client c WHERE c.isActive = true OR c.isActive IS NULL")
    long countActiveClients();
}

