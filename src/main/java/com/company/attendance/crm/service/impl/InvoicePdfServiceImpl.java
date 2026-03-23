package com.company.attendance.crm.service.impl;

import com.company.attendance.crm.entity.Invoice;
import com.company.attendance.crm.service.InvoicePdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private final FreeMarkerConfigurer freeMarkerConfigurer;

    public InvoicePdfServiceImpl(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.freeMarkerConfigurer = freeMarkerConfigurer;
    }

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            // 1. Process HTML template with invoice data
            String htmlContent = processHtmlTemplate(invoice);
            
            // 2. Convert HTML to PDF using Flying Saucer
            return convertHtmlToPdf(htmlContent);
            
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice {}", invoice.getInvoiceNo(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private String processHtmlTemplate(Invoice invoice) throws Exception {
        Map<String, Object> model = new HashMap<>();
        
        // Invoice details
        model.put("invoiceNo", invoice.getInvoiceNo());
        model.put("invoiceDate", formatDate(invoice.getInvoiceDate()));
        model.put("dueDate", formatDate(invoice.getDueDate()));
        model.put("isProForma", invoice.getIsProForma() != null ? invoice.getIsProForma() : false);
        model.put("includeGst", invoice.getIncludeGst() != null ? invoice.getIncludeGst() : false);
        
        // Company details
        model.put("billedByName", invoice.getBilledByName());
        model.put("billedByAddress", invoice.getBilledByAddress());
        model.put("billedByEmail", invoice.getBilledByEmail());
        model.put("gstin", invoice.getGstin());
        model.put("pan", invoice.getPan());
        model.put("companyLogo", invoice.getCompanyLogo());
        model.put("signature", invoice.getSignature());
        
        // Customer details
        model.put("billedToName", invoice.getBilledToName());
        model.put("billedToAddress", invoice.getBilledToAddress());
        model.put("billedToEmail", invoice.getBilledToEmail());
        model.put("billedToMobile", invoice.getBilledToMobile());
        model.put("billedToGstin", invoice.getBilledToGstin());
        
        // Items
        model.put("items", invoice.getItems());
        
        // Calculate totals like the frontend
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            BigDecimal subtotal = invoice.getItems().stream()
                .map(item -> {
                    BigDecimal rate = item.getRate();
                    BigDecimal qty = item.getQty();
                    return rate.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double subtotalDouble = subtotal.doubleValue();
            double cgst = invoice.getIncludeGst() != null && invoice.getIncludeGst() ? subtotalDouble * 0.09 : 0;
            double sgst = invoice.getIncludeGst() != null && invoice.getIncludeGst() ? subtotalDouble * 0.09 : 0;
            double grandTotal = subtotalDouble + cgst + sgst;
            
            model.put("subtotal", formatCurrency(subtotalDouble));
            model.put("cgst", formatCurrency(cgst));
            model.put("sgst", formatCurrency(sgst));
            model.put("grandTotal", formatCurrency(grandTotal));
        } else {
            model.put("subtotal", "0.00");
            model.put("cgst", "0.00");
            model.put("sgst", "0.00");
            model.put("grandTotal", "0.00");
        }
        
        // Bank details
        model.put("accountName", invoice.getAccountName());
        model.put("accountNumber", invoice.getAccountNumber());
        model.put("accountType", invoice.getAccountType());
        model.put("bank", invoice.getBank());
        model.put("ifsc", invoice.getIfsc());
        
        // UPI details and QR code
        model.put("upiId", invoice.getUpiId());
        if (invoice.getUpiId() != null && invoice.getAccountName() != null) {
            String upiUri = String.format("upi://pay?pa=%s&pn=%s&am=%s&cu=INR&tn=Invoice Payment",
                invoice.getUpiId().trim(),
                invoice.getAccountName().trim(),
                invoice.getGrandTotal() != null ? invoice.getGrandTotal().toString() : "0");
            model.put("upiUri", upiUri);
        }
        
        // Terms
        model.put("terms", invoice.getTerms() != null ? invoice.getTerms() : "Payment due within 30 days. Late payment charges may apply.");
        
        // Process template
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            freeMarkerConfigurer.getConfiguration().getTemplate("invoice-template.html"),
            model
        );
    }

    private byte[] convertHtmlToPdf(String htmlContent) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // Create PDF renderer
            org.xhtmlrenderer.pdf.ITextRenderer renderer = new org.xhtmlrenderer.pdf.ITextRenderer();
            
            // Set HTML content
            renderer.setDocumentFromString(htmlContent);
            
            // Configure renderer for better quality
            renderer.getSharedContext().setBaseURL("http://localhost:8080");
            renderer.getSharedContext().setInteractive(false);
            renderer.getSharedContext().setDPI(96);
            
            // Layout and render
            renderer.layout();
            renderer.createPDF(outputStream);
            
            byte[] pdfBytes = outputStream.toByteArray();
            log.info("Generated PDF size = {} bytes", pdfBytes.length);
            
            return pdfBytes;
        }
    }

    private String formatDate(java.time.Instant date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        return sdf.format(java.util.Date.from(date));
    }
    
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount).replace("₹", "₹");
    }
}
