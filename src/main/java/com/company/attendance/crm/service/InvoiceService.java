package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Invoice;
import com.company.attendance.crm.entity.InvoiceItem;
import com.company.attendance.crm.dto.InvoiceDto;
import com.company.attendance.crm.mapper.InvoiceMapper;
import com.company.attendance.crm.repository.InvoiceRepository;
import com.company.attendance.crm.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final AuditService auditService;
    
    public Page<Invoice> list(Pageable pageable) {
        log.info("Getting invoices with pagination: {}", pageable);
        return invoiceRepository.findAll(pageable);
    }
    
    public Invoice get(Long id) {
        log.info("Getting invoice by id: {}", id);
        return invoiceRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }
    
    public Invoice create(InvoiceDto dto) {
        log.info("Creating new invoice: {}", dto.getInvoiceNo());
        Invoice invoice = invoiceMapper.toInvoiceEntity(dto);
        
        // Set audit fields
        auditService.setAuditFields(invoice);
        
        // Calculate totals if items are provided
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            calculateInvoiceTotals(invoice, dto.getItems());
        }
        
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Successfully created invoice with id: {}", saved.getId());
        return saved;
    }
    
    public Invoice update(Long id, InvoiceDto dto) {
        log.info("Updating invoice: {}", id);
        Invoice existing = get(id);
        
        // Update fields
        existing.setInvoiceNo(dto.getInvoiceNo());
        existing.setInvoiceDate(dto.getInvoiceDate());
        existing.setDueDate(dto.getDueDate());
        existing.setIsProForma(dto.getIsProForma());
        existing.setIncludeGst(dto.getIncludeGst());
        existing.setBilledByName(dto.getBilledByName());
        existing.setBilledByAddress(dto.getBilledByAddress());
        existing.setBilledByEmail(dto.getBilledByEmail());
        existing.setGstin(dto.getGstin());
        existing.setPan(dto.getPan());
        existing.setBilledToName(dto.getBilledToName());
        existing.setBilledToAddress(dto.getBilledToAddress());
        existing.setBilledToGstin(dto.getBilledToGstin());
        existing.setBilledToMobile(dto.getBilledToMobile());
        existing.setBilledToEmail(dto.getBilledToEmail());
        existing.setAccountName(dto.getAccountName());
        existing.setAccountNumber(dto.getAccountNumber());
        existing.setIfsc(dto.getIfsc());
        existing.setAccountType(dto.getAccountType());
        existing.setBank(dto.getBank());
        existing.setUpiId(dto.getUpiId());
        existing.setTerms(dto.getTerms());
        existing.setStatus(dto.getStatus());
        
        // Update logo and signature only if provided (not null/empty)
        if (dto.getCompanyLogo() != null && !dto.getCompanyLogo().trim().isEmpty()) {
            existing.setCompanyLogo(dto.getCompanyLogo());
        }
        if (dto.getSignature() != null && !dto.getSignature().trim().isEmpty()) {
            existing.setSignature(dto.getSignature());
        }
        
        // Update items if provided
        if (dto.getItems() != null) {
            existing.getItems().clear(); // Clear old items first
            
            List<InvoiceItem> items = dto.getItems().stream()
                .map(itemDto -> {
                    InvoiceItem item = invoiceMapper.toInvoiceItemEntity(itemDto);
                    item.setInvoice(existing); // Set the invoice reference
                    return item;
                })
                .collect(Collectors.toList());
            existing.setItems(items);
        }
        
        // Update audit fields
        auditService.updateAuditFields(existing);
        
        // Recalculate totals if items are provided
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            calculateInvoiceTotals(existing, dto.getItems());
        }
        
        Invoice updated = invoiceRepository.save(existing);
        log.info("Successfully updated invoice: {}", updated.getId());
        return updated;
    }
    
    public void delete(Long id) {
        log.info("Deleting invoice: {}", id);
        Invoice invoice = get(id);
        invoiceRepository.delete(invoice);
        log.info("Successfully deleted invoice: {}", id);
    }
    
    // 📧 Get invoice entity for email/pdf operations
    public Invoice getInvoiceEntity(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }
    
    private void calculateInvoiceTotals(Invoice invoice, List<com.company.attendance.crm.dto.InvoiceItemDto> items) {
        java.math.BigDecimal subtotal = items.stream()
                .map(item -> item.getRate().multiply(item.getQty()))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        java.math.BigDecimal cgst = invoice.getIncludeGst() ? 
                subtotal.multiply(java.math.BigDecimal.valueOf(0.09)) : java.math.BigDecimal.ZERO;
        java.math.BigDecimal sgst = invoice.getIncludeGst() ? 
                subtotal.multiply(java.math.BigDecimal.valueOf(0.09)) : java.math.BigDecimal.ZERO;
        java.math.BigDecimal grandTotal = subtotal.add(cgst).add(sgst);
        
        invoice.setSubtotal(subtotal);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setGrandTotal(grandTotal);
        invoice.setAmountInWords(convertToWords(grandTotal));
    }
    
    private String convertToWords(java.math.BigDecimal amount) {
        // Simple implementation - you can use a proper number-to-words library
        if (amount.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "ZERO";
        }
        // For now, return the amount as string
        return "RUPEES " + amount.toString() + " ONLY";
    }
}
