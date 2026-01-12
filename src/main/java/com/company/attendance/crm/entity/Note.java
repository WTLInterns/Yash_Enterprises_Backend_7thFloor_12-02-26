package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    private String title;

    @Lob
    private String body;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
