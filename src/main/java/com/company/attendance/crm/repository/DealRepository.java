package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    
    List<Deal> findByClientId(Long clientId);
    
    Optional<Deal> findFirstByClientIdOrderByCreatedAtDesc(Long clientId);
    
    @Query("SELECT d FROM Deal d WHERE d.client.id = :clientId")
    List<Deal> findByClientEntityId(@Param("clientId") Long clientId);
    
    default Deal findByIdSafe(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + id));
    }
}
