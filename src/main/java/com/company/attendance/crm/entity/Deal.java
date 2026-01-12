package com.company.attendance.crm.entity;

import com.company.attendance.crm.enums.DealStage;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Data
public class Deal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    // Optional: store selected bank
    @Column(name = "bank_id")
    private Long bankId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", insertable = false, updatable = false)
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private com.company.attendance.entity.Client client;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "value_amount")
    private BigDecimal valueAmount;
    
    @Column(name = "closing_date")
    private LocalDate closingDate;
    
    @Column(name = "branch_name")
    private String branchName;
    
    @Column(name = "related_bank_name")
    private String relatedBankName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "required_amount")
    private BigDecimal requiredAmount;
    
    @Column(name = "outstanding_amount")
    private BigDecimal outstandingAmount;
    
    @Enumerated(EnumType.STRING)
    private DealStage stage;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "updated_by")
    private Integer updatedBy;
}
