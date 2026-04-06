package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "bank_email_logs")
@Data
public class BankEmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_id", nullable = false)
    private Long bankId;

    @Column(name = "to_email")
    private String toEmail;

    @Column(name = "cc_email")
    private String ccEmail;

    private String subject;

    @Lob
    private String body;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @Column(name = "sent_by")
    private Long sentBy;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    private String status; // SENT, FAILED
}
