package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "field_definitions")
@Data
public class FieldDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entity; // "bank", "client", "product", "deal"
    
    @Column(name = "field_key", nullable = false, unique = true)
    private String fieldKey;
    
    @Column(name = "field_name", nullable = false)
    private String fieldName;
    
    @Column(name = "field_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;
    
    @Column(nullable = false)
    private Boolean required = false;
    
    @Column(name = "options_json", columnDefinition = "json")
    private String optionsJson;
    
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
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    public enum FieldType {
        TEXT, NUMBER, DATE, SELECT, BOOLEAN, TEXTAREA
    }
}
