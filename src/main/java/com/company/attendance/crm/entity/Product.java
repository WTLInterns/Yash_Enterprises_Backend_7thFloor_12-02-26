package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Data
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String code;
    private BigDecimal price;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private com.company.attendance.crm.entity.ProductCategory category;
    
    private String description;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @Column(name="custom_fields", columnDefinition="json")
    private String customFields;
    
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
