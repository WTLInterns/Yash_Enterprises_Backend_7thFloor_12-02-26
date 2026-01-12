package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "product_lines")
public class ProductLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    private String productName;
    private BigDecimal listPrice;
    private BigDecimal quantity;
    private BigDecimal discount;
    private BigDecimal total;

    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "modified_by")
    private Integer modifiedBy;
    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @PrePersist
    public void prePersist(){
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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(Integer modifiedBy) { this.modifiedBy = modifiedBy; }
    public OffsetDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(OffsetDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
}
