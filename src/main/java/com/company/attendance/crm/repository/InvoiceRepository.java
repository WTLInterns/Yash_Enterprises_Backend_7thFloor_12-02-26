package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findAll(Pageable pageable);
    Optional<Invoice> findByInvoiceNo(String invoiceNo);
    List<Invoice> findByStatus(String status);
    List<Invoice> findByBilledToNameContainingIgnoreCase(String billedToName);
    
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(Long id);
}
