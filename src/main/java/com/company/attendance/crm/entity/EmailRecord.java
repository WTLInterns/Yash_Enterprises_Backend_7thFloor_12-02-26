package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_records")
public class EmailRecord {
    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    private String toAddress;
    private String ccAddress;
    private String subject;

    @Lob
    private String body;

    @Column(name = "sent_by", length = 36)
    private UUID sentBy;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    private String status; // SENT, FAILED, DRAFT

    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
    }

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }
    public String getCcAddress() { return ccAddress; }
    public void setCcAddress(String ccAddress) { this.ccAddress = ccAddress; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public UUID getSentBy() { return sentBy; }
    public void setSentBy(UUID sentBy) { this.sentBy = sentBy; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
