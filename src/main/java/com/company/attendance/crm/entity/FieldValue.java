package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "field_values")
@Data
public class FieldValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entity; // "bank", "client", "product", "deal"
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "field_key", nullable = false)
    private String fieldKey;
    
    @Column(columnDefinition = "TEXT")
    private String value;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "updated_by")
    private Integer updatedBy;
}
