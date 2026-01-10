package com.company.attendance.repository;

import com.company.attendance.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    
    Optional<Client> findByEmail(String email);
    
    List<Client> findByIsActive(Boolean isActive);
    
    @Query("SELECT COUNT(c) FROM Client c WHERE c.isActive = true")
    long countActiveClients();
}

