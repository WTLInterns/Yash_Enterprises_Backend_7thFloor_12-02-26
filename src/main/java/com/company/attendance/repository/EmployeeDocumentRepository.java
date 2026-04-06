package com.company.attendance.repository;

import com.company.attendance.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    List<EmployeeDocument> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    void deleteByEmployeeId(Long employeeId);
}
