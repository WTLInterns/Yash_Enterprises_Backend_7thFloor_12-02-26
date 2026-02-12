package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@Getter
@Setter
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String invoiceNo;
    
    @Column(name = "invoice_date", nullable = false)
    private Instant invoiceDate;
    
    @Column(name = "due_date", nullable = false)
    private Instant dueDate;
    
    @Column(name = "is_pro_forma")
    private Boolean isProForma = false;
    
    @Column(name = "include_gst")
    private Boolean includeGst = true;
    
    // Billed By Details
    @Column(name = "billed_by_name", nullable = false)
    private String billedByName;
    
    @Column(name = "billed_by_address")
    private String billedByAddress;
    
    @Column(name = "billed_by_email")
    private String billedByEmail;
    
    private String gstin;
    private String pan;
    
    // Billed To Details
    @Column(name = "billed_to_name", nullable = false)
    private String billedToName;
    
    @Column(name = "billed_to_address")
    private String billedToAddress;
    
    @Column(name = "billed_to_gstin")
    private String billedToGstin;
    
    @Column(name = "billed_to_mobile")
    private String billedToMobile;
    
    @Column(name = "billed_to_email")
    private String billedToEmail;
    
    // Bank Details
    @Column(name = "account_name")
    private String accountName;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    private String ifsc;
    
    @Column(name = "account_type")
    private String accountType;
    
    private String bank;
    
    // UPI Details
    @Column(name = "upi_id")
    private String upiId;
    
    @Column(columnDefinition = "TEXT")
    private String terms;
    
    // Logo and Signature
    @Column(columnDefinition = "TEXT")
    private String companyLogo;
    
    @Column(columnDefinition = "TEXT")
    private String signature;
    
    // Calculated Fields
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cgst;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal sgst;
    
    @Column(name = "grand_total", precision = 10, scale = 2)
    private BigDecimal grandTotal;
    
    @Column(name = "amount_in_words", columnDefinition = "TEXT")
    private String amountInWords;
    
    // Status
    private String status = "DRAFT";
    
    // 📧 Email sent timestamp
    @Column(name = "sent_at")
    private Instant sentAt;
    
    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Relationships
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> items;
}
