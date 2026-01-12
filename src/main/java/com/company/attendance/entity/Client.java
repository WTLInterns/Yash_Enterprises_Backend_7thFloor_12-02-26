package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@EqualsAndHashCode(callSuper = false)
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String contactPhone;
    private String address;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name="custom_fields", columnDefinition="json")
    private String customFields;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    private Long updatedBy;
    private Long ownerId;
}
