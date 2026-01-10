package com.company.attendance.crm.entity;

import com.company.attendance.crm.enums.DealStage;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "deals")
public class Deal {
    @Id
    @Column(length = 36)
    private UUID id;

    @Column(name = "client_id", columnDefinition = "BINARY(16)")
    private UUID clientId;

    @Column(nullable = false)
    private String name;

    @Column(name = "value_amount")
    private BigDecimal valueAmount;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "related_bank_name")
    private String relatedBankName;

    @Lob
    private String description;

    @Column(name = "required_amount")
    private BigDecimal requiredAmount;

    @Column(name = "outstanding_amount")
    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    private DealStage stage;

    @Column(name = "owner_id", length = 36)
    private UUID ownerId;

    @Column(name = "bank_id", length = 36)
    private UUID bankId;

    @Column(name = "created_by", length = 36)
    private UUID createdBy;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "modified_by", length = 36)
    private UUID modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @Column(name = "active")
    private Boolean active = true;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (stage == null) stage = DealStage.LEAD;
        if (active == null) active = true;
    }

    @PreUpdate
    public void preUpdate() {
        modifiedAt = OffsetDateTime.now();
    }

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public boolean isActive() { return active != null && active; }

    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getValueAmount() { return valueAmount; }
    public void setValueAmount(BigDecimal valueAmount) { this.valueAmount = valueAmount; }
    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getRelatedBankName() { return relatedBankName; }
    public void setRelatedBankName(String relatedBankName) { this.relatedBankName = relatedBankName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getRequiredAmount() { return requiredAmount; }
    public void setRequiredAmount(BigDecimal requiredAmount) { this.requiredAmount = requiredAmount; }
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }
    public DealStage getStage() { return stage; }
    public void setStage(DealStage stage) { this.stage = stage; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(UUID modifiedBy) { this.modifiedBy = modifiedBy; }
    public OffsetDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(OffsetDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
}
