package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    @Column(name = "stage_code", nullable = false)
    private String stageCode;
    
    @Column(name = "department", nullable = false)
    private String department;
    
    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "moved_to_approval")
    private Boolean movedToApproval = false;

    @Column(name = "deal_code", unique = true)
    private String dealCode;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<DealStageHistory> stageHistory;
    
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<DealProduct> dealProducts;
    
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Note> notes;
    
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Activity> activities;

    // Initialize lists to avoid NullPointerException
    public Deal() {
        this.stageHistory = new ArrayList<>();
        this.dealProducts = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.activities = new ArrayList<>();
    }
}
