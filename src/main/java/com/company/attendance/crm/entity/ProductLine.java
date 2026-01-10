package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_lines")
public class ProductLine {
    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    private String productName;
    private BigDecimal listPrice;
    private BigDecimal quantity;
    private BigDecimal discount;
    private BigDecimal total;

    @Column(name = "created_by", length = 36)
    private UUID createdBy;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "modified_by", length = 36)
    private UUID modifiedBy;
    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        computeTotal();
    }

    @PreUpdate
    public void preUpdate(){
        modifiedAt = OffsetDateTime.now();
        computeTotal();
    }

    public void computeTotal(){
        BigDecimal price = listPrice != null ? listPrice : BigDecimal.ZERO;
        BigDecimal qty = quantity != null ? quantity : BigDecimal.ZERO;
        BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
        this.total = price.multiply(qty).subtract(disc);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getListPrice() { return listPrice; }
    public void setListPrice(BigDecimal listPrice) { this.listPrice = listPrice; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(UUID modifiedBy) { this.modifiedBy = modifiedBy; }
    public OffsetDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(OffsetDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
}
