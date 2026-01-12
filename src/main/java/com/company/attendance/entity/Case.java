package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cases")
@Data
@EqualsAndHashCode(callSuper = false)
public class Case {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "case_number", nullable = false, unique = true)
    private String caseNumber;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private CaseStatus status;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    @Column(name = "client_id")
    private Long clientId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;
    
    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CaseDocument> documents;
    
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
    
    public enum CaseStatus {
        OPEN, IN_PROGRESS, CLOSED, PENDING, COMPLETED
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
