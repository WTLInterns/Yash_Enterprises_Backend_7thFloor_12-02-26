package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bank_contact_persons")
@Data
public class BankContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    private String email;
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
}
