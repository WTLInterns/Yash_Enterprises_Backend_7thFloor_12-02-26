package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.Invoice;
import com.company.attendance.crm.repository.InvoiceRepository;
import com.company.attendance.crm.service.InvoicePdfService;
import com.company.attendance.crm.service.InvoiceService;
import com.company.attendance.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceEmailController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;
    private final EmailService emailService;
    private final InvoiceRepository invoiceRepository;

    // 📧 SEND INVOICE EMAIL
    @PostMapping("/{invoiceId}/send")
    public ResponseEntity<Map<String, Object>> sendInvoiceEmail(
            @PathVariable Long invoiceId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Invoice invoice = invoiceService.getInvoiceEntity(invoiceId);

            if (invoice.getBilledToEmail() == null || invoice.getBilledToEmail().isBlank()) {
                response.put("success", false);
                response.put("message", "Customer email not found");
                return ResponseEntity.badRequest().body(response);
            }

            // ✅ Generate PDF
            byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice);

            // ✅ Send email with attachment
            emailService.sendInvoiceEmail(
                    invoice.getBilledToEmail(),
                    invoice.getBilledToName(),
                    invoice.getInvoiceNo(),
                    pdfBytes
            );

            // ✅ Update invoice status
            invoice.setStatus("SENT");
            invoice.setSentAt(Instant.now());
            invoiceRepository.save(invoice);

            response.put("success", true);
            response.put("message", "Invoice sent successfully");
            response.put("status", "SENT");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send invoice: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
