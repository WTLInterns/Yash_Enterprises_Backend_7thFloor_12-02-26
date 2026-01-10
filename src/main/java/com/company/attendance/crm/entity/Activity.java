package com.company.attendance.crm.entity;

import com.company.attendance.crm.enums.ActivityStatus;
import com.company.attendance.crm.enums.ActivityType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    @JsonIgnore
    private Deal deal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(name = "owner_id", length = 36)
    private UUID ownerId;

    @Column(name = "due_date")
    private OffsetDateTime dueDate; // for TASK

    @Column(name = "start_date")
    private OffsetDateTime startDate; // for EVENT/CALL

    @Column(name = "end_date")
    private OffsetDateTime endDate; // for EVENT/CALL

    @Enumerated(EnumType.STRING)
    private ActivityStatus status = ActivityStatus.PENDING;

    private String priority; // LOW, MEDIUM, HIGH (string for flexibility)

    @Column(name = "repeat_rule")
    private String repeatRule;

    private OffsetDateTime reminder;

    @Column(name = "created_by", length = 36)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "modified_by", length = 36)
    private UUID modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = ActivityStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        modifiedAt = OffsetDateTime.now();
    }

    // Getters & setters omitted for brevity
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
    public ActivityStatus getStatus() { return status; }
    public void setStatus(ActivityStatus status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getRepeatRule() { return repeatRule; }
    public void setRepeatRule(String repeatRule) { this.repeatRule = repeatRule; }
    public OffsetDateTime getReminder() { return reminder; }
    public void setReminder(OffsetDateTime reminder) { this.reminder = reminder; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(UUID modifiedBy) { this.modifiedBy = modifiedBy; }
    public OffsetDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(OffsetDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
}
