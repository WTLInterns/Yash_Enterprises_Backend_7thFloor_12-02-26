package com.company.attendance.repository;

import com.company.attendance.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, Long> {
    
    @Query("SELECT f FROM Form f WHERE f.isActive = true ORDER BY f.createdAt DESC")
    List<Form> findAllActiveOrderByCreatedAtDesc();
    
    @Query("SELECT f FROM Form f WHERE f.isActive = true AND f.clientId = :clientId ORDER BY f.createdAt DESC")
    List<Form> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    @Query("SELECT f FROM Form f WHERE f.isActive = true AND f.createdBy = :createdBy ORDER BY f.createdAt DESC")
    List<Form> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
}

