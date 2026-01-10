package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "deal_products")
public class DealProduct {
    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "unit_price")
    private BigDecimal unitPrice; // snapshot

    private BigDecimal quantity;
    private BigDecimal discount; // absolute
    private BigDecimal tax; // absolute
    private BigDecimal total; // computed

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
        computeTotal();
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = OffsetDateTime.now();
        computeTotal();
    }

    public void computeTotal(){
        BigDecimal price = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        BigDecimal qty = quantity != null ? quantity : BigDecimal.ZERO;
        BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal tx = tax != null ? tax : BigDecimal.ZERO;
        this.total = price.multiply(qty).subtract(disc).add(tx);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
