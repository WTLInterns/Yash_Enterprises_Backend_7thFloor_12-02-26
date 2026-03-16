package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    Optional<Deal> findByNameAndClientId(String name, Long clientId);
    
    @Query("SELECT d FROM Deal d WHERE d.client.id = :clientId")
    List<Deal> findByClientEntityId(@Param("clientId") Long clientId);
    
    @Query("SELECT d FROM Deal d WHERE d.department = :department AND d.stageCode = :stage")
    List<Deal> findByDepartmentAndStage(@Param("department") String department, @Param("stage") String stage);
    
    @Query("SELECT d FROM Deal d WHERE d.department = :department")
    List<Deal> findByDepartment(@Param("department") String department);
    
    /**
     * 🔥 DEPARTMENT-AWARE: Find deals by department with pagination
     */
    @Query("SELECT d FROM Deal d WHERE d.department = :department")
    Page<Deal> findByDepartment(@Param("department") String department, Pageable pageable);
    
    @Query("SELECT d FROM Deal d WHERE d.stageCode = :stage")
    List<Deal> findByStage(@Param("stage") String stage);
    
    default Deal findByIdSafe(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + id));
    }
}
