package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
public class FileMeta {
    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private Deal deal; // nullable: can be linked to note or deal

    @Column(name = "uploaded_by", length = 36)
    private UUID uploadedBy;

    private String fileName;
    private Long fileSize;
    private String contentType;
    private String storagePath;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public UUID getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
