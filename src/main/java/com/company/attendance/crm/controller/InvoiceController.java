package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.InvoiceDto;
import com.company.attendance.crm.service.InvoiceService;
import com.company.attendance.crm.mapper.InvoiceMapper;
import com.company.attendance.crm.service.AuditService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Invoices")
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;
    private final AuditService auditService;

    @GetMapping
    public Page<InvoiceDto> list(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Pageable pageable) {
        log.info("GET /api/invoices - Getting invoices list with search: {}, page: {}, size: {}", search, page, size);
        
        Page<com.company.attendance.crm.entity.Invoice> invoices;
        if (search != null && !search.trim().isEmpty()) {
            // For now, implement basic search by billed to name
            // You can extend this to search multiple fields
            invoices = invoiceService.list(pageable);
        } else {
            invoices = invoiceService.list(pageable);
        }
        
        List<InvoiceDto> dtos = invoices.getContent().stream()
                .map(invoice -> {
                    InvoiceDto dto = invoiceMapper.toInvoiceDto(invoice);
                    dto.setCreatedByName(getUserName(invoice.getCreatedBy()));
                    dto.setUpdatedByName(getUserName(invoice.getUpdatedBy()));
                    return dto;
                })
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, invoices.getTotalElements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> get(@PathVariable Long id) {
        log.info("GET /api/invoices/{} - Fetching invoice", id);
        try {
            com.company.attendance.crm.entity.Invoice invoice = invoiceService.get(id);
            InvoiceDto dto = invoiceMapper.toInvoiceDto(invoice);
            dto.setCreatedByName(getUserName(invoice.getCreatedBy()));
            dto.setUpdatedByName(getUserName(invoice.getUpdatedBy()));
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error fetching invoice {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> create(@Valid @RequestBody InvoiceDto invoiceDto) {
        log.info("POST /api/invoices - Creating invoice: {}", invoiceDto.getInvoiceNo());
        try {
            com.company.attendance.crm.entity.Invoice created = invoiceService.create(invoiceDto);
            InvoiceDto response = invoiceMapper.toInvoiceDto(created);
            response.setCreatedByName(auditService.getCurrentUserName());
            return ResponseEntity.created(URI.create("/api/invoices/" + created.getId())).body(response);
        } catch (Exception e) {
            log.error("Error creating invoice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> update(@PathVariable Long id, @Valid @RequestBody InvoiceDto invoiceDto) {
        log.info("PUT /api/invoices/{} - Updating invoice", id);
        try {
            com.company.attendance.crm.entity.Invoice updated = invoiceService.update(id, invoiceDto);
            InvoiceDto response = invoiceMapper.toInvoiceDto(updated);
            response.setUpdatedByName(auditService.getCurrentUserName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating invoice {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/invoices/{} - Deleting invoice", id);
        try {
            invoiceService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting invoice {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        log.info("GET /api/invoices/stats - Getting invoice statistics");
        try {
            // For now, return mock stats - you can implement real stats
            Object stats = java.util.Map.of(
                "total", 0,
                "thisMonth", 0,
                "totalAmount", 0,
                "pending", 0
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting invoice stats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private String getUserName(Long userId) {
        if (userId == null) return "System";
        return auditService.getUserName(userId);
    }
}
