package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Form title
    private String name;

    // Form description
    @Column(columnDefinition = "TEXT")
    private String description;

    // JSON schema (Questions + Settings)
    @Column(name = "`schema`", columnDefinition = "LONGTEXT")
    private String schema;

    // ✅ client mapping
    private Long clientId;

    private Long createdBy;
    private Long updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isActive;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isActive = (isActive == null) ? true : isActive;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
