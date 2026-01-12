package com.company.attendance.crm.entity;

import com.company.attendance.crm.enums.DealStage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "deal_stage_history")
public class DealStageHistory {
    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    @JsonIgnore
    private Deal deal;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_stage")
    private DealStage previousStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_stage")
    private DealStage newStage;

    @Column(name = "changed_by", length = 36)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @Column(name = "note")
    private String note;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (changedAt == null) changedAt = OffsetDateTime.now();
    }

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public DealStage getPreviousStage() { return previousStage; }
    public void setPreviousStage(DealStage previousStage) { this.previousStage = previousStage; }
    public DealStage getNewStage() { return newStage; }
    public void setNewStage(DealStage newStage) { this.newStage = newStage; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public OffsetDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(OffsetDateTime changedAt) { this.changedAt = changedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
