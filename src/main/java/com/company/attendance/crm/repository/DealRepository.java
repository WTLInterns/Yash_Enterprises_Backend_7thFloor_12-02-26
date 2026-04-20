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

    List<Deal> findByBankId(Long bankId);

    List<Deal> findAllByClientIdIn(List<Long> clientIds);
    
    Optional<Deal> findFirstByClientIdOrderByCreatedAtDesc(Long clientId);
    
    Optional<Deal> findByNameAndClientId(String name, Long clientId);
    
    @Query("SELECT d FROM Deal d WHERE d.client.id = :clientId")
    List<Deal> findByClientEntityId(@Param("clientId") Long clientId);
    
    @Query("SELECT d FROM Deal d WHERE d.department = :department AND d.stageCode = :stage")
    List<Deal> findByDepartmentAndStage(@Param("department") String department, @Param("stage") String stage);
    
    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank WHERE d.department = :department")
    List<Deal> findByDepartment(@Param("department") String department);
    
    /**
     * 🔥 DEPARTMENT-AWARE: Find deals by department with pagination
     */
    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank WHERE d.department = :department")
    Page<Deal> findByDepartment(@Param("department") String department, Pageable pageable);
    
    @Query("SELECT d FROM Deal d WHERE d.stageCode = :stage")
    List<Deal> findByStage(@Param("stage") String stage);

    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank WHERE d.department = :dept AND (d.movedToApproval = false OR d.movedToApproval IS NULL)")
    List<Deal> findByDepartmentNotMovedToApproval(@Param("dept") String dept);

    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank WHERE d.movedToApproval = true")
    List<Deal> findMovedToApproval();

    @Query("SELECT COUNT(d) FROM Deal d WHERE d.department = :dept")
    long countByDepartment(@Param("dept") String dept);

    boolean existsByDealCode(String dealCode);

    Optional<Deal> findByDealCodeIgnoreCase(String dealCode);

    @Query("SELECT d FROM Deal d WHERE d.clientId = :clientId AND d.department = :department")
    List<Deal> findByClientIdAndDepartment(@Param("clientId") Long clientId, @Param("department") String department);

    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank WHERE d.clientId = :clientId")
    List<Deal> findByClientIdWithRelations(@Param("clientId") Long clientId);

    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank WHERE d.id = :id")
    Optional<Deal> findByIdWithRelations(@Param("id") Long id);

    @Query(value = "SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank",
           countQuery = "SELECT COUNT(d) FROM Deal d")
    Page<Deal> findAllWithClient(Pageable pageable);

    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank")
    List<Deal> findAllWithClient();

    @Query("SELECT DISTINCT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank LEFT JOIN FETCH d.dealProducts dp LEFT JOIN FETCH dp.product")
    List<Deal> findAllWithClientAndProducts();

    @Query("SELECT DISTINCT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank LEFT JOIN FETCH d.dealProducts dp LEFT JOIN FETCH dp.product WHERE d.department = :department")
    List<Deal> findByDepartmentWithProducts(@Param("department") String department);

    @Query("SELECT DISTINCT d FROM Deal d LEFT JOIN FETCH d.client LEFT JOIN FETCH d.bank LEFT JOIN FETCH d.dealProducts dp LEFT JOIN FETCH dp.product")
    List<Deal> findAllWithClientAndProductsFull();

    default Deal findByIdSafe(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + id));
    }
}
