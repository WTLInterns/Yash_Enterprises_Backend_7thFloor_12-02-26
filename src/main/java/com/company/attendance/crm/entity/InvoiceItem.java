package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoice_items")
@Data
@Getter
@Setter
public class InvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal rate;
    
    @Column(nullable = false)
    private BigDecimal qty;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cgst;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal sgst;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    
    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
