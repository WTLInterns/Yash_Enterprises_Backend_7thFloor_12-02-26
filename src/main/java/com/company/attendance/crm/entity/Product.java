package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_name", columnList = "product_name", unique = true),
        @Index(name = "idx_products_code", columnList = "product_code")
})
public class Product {
    @Id
    @Column(length = 36)
    private UUID id;

    @Column(name = "product_name", nullable = false, unique = true)
    private String productName;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "category_id", length = 36)
    private UUID categoryId; // managed category FK (optional)

    @Lob
    private String description;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "owner_id", length = 36)
    private UUID ownerId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
        if (active == null) active = true;
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
