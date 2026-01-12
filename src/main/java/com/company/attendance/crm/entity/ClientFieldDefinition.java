package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_field_definitions")
@Data
public class ClientFieldDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fieldKey;
    
    @Column(nullable = false)
    private String fieldName;
    
    @Column(nullable = false)
    private String fieldType;
    
    @Column(name = "options_json", columnDefinition = "json")
    private String optionsJson;
    
    private Boolean required = false;
    
    private Boolean active = true;
    
    private Integer orderIndex = 0;
    
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
