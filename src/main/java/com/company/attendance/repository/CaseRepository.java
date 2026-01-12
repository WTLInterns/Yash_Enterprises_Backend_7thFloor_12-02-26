package com.company.attendance.repository;

import com.company.attendance.entity.Case;
import com.company.attendance.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    
    List<Case> findByClient(Client client);
    
    List<Case> findByClientId(Long clientId);
    
    List<Case> findByClientAndStatus(Client client, String status);
    
    Optional<Case> findByCaseNumber(String caseNumber);
}
