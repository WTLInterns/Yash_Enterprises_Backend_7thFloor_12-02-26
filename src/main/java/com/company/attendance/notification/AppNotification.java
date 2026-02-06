package com.company.attendance.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientEmployeeId;

    @Column(nullable = false, length = 32)
    private String channel; // MOBILE / WEB

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(length = 64)
    private String type; // TASK_ASSIGNED / TASK_STATUS / FORM_UPDATED etc

    @Column(length = 64)
    private String refType; // TASK / FORM

    private Long refId;

    @Column(columnDefinition = "TEXT")
    private String dataJson;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
