package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Integer> {
    
    List<Deal> findByClientId(Long clientId);
    
    Optional<Deal> findFirstByClientIdOrderByCreatedAtDesc(Long clientId);
    
    @Query("SELECT d FROM Deal d WHERE d.client.id = :clientId")
    List<Deal> findByClientEntityId(@Param("clientId") Long clientId);
    
    default Deal findByIdSafe(Integer id) {
        return findById(id).orElseThrow(() ->
            new RuntimeException("Deal not found: " + id)
        );
    }
}
