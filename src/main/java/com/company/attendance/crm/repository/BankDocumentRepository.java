package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.BankDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankDocumentRepository extends JpaRepository<BankDocument, Long> {
    List<BankDocument> findByBankIdOrderByCreatedAtDesc(Long bankId);
    void deleteByBankId(Long bankId);
}
