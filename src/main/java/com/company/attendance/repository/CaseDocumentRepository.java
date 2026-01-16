package com.company.attendance.repository;

import com.company.attendance.entity.CaseDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseDocumentRepository extends JpaRepository<CaseDocument, Long> {
    
    List<CaseDocument> findByCaseEntityId(Long caseId);
    
    Optional<CaseDocument> findByFileNameAndCaseEntityId(String fileName, Long caseId);
    
    Long countByCaseEntityId(Long caseId);

    void deleteByCaseEntityId(Long caseId);
}
