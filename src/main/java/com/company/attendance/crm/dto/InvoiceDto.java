package com.company.attendance.crm.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class InvoiceDto {
    private Long id;
    private String invoiceNo;
    private Instant invoiceDate;
    private Instant dueDate;
    private Boolean isProForma;
    private Boolean includeGst;
    
    // Billed By Details
    private String billedByName;
    private String billedByAddress;
    private String billedByEmail;
    private String gstin;
    private String pan;
    
    // Billed To Details
    private String billedToName;
    private String billedToAddress;
    private String billedToGstin;
    private String billedToMobile;
    private String billedToEmail;
    
    // Bank Details
    private String accountName;
    private String accountNumber;
    private String ifsc;
    private String accountType;
    private String bank;
    
    // UPI Details
    private String upiId;
    private String terms;
    
    // Logo and Signature
    private String companyLogo;
    private String signature;
    
    // Calculated Fields
    private BigDecimal subtotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal grandTotal;
    private String amountInWords;
    
    // Status
    private String status;
    
    // 📧 Email sent timestamp
    private Instant sentAt;
    
    // Items
    private List<InvoiceItemDto> items;
    
    // Audit Fields
    private Instant createdAt;
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;
    
    // Additional fields for UI
    private String createdByName;
    private String updatedByName;
}
