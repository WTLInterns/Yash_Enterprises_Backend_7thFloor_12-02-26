package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_price_history")
public class ProductPriceHistory {
    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "old_price")
    private BigDecimal oldPrice;

    @Column(name = "new_price")
    private BigDecimal newPrice;

    @Column(name = "changed_by", length = 36)
    private UUID changedBy;

    @Column(name = "changed_at")
    private OffsetDateTime changedAt;

    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
        if (changedAt == null) changedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }
    public UUID getChangedBy() { return changedBy; }
    public void setChangedBy(UUID changedBy) { this.changedBy = changedBy; }
    public OffsetDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(OffsetDateTime changedAt) { this.changedAt = changedAt; }
}
