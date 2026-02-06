package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "banks")
@Data
@Getter
@Setter
public class Bank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String code;
    
    private String logo;
    private String address;
    private String city;
    private String state;
    private String country;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String industry;
    private String description;
    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String currency;
    private String taxId;
    private String registrationNumber;
    private String fiscalYearStart;
    private String fiscalYearEnd;
    private String primaryColor;
    private String secondaryColor;
    
    private String bankName;
    private String branchName;
    
    @Column(name="phone")
    private String phone;
    private String district;
    private String taluka;

    @Column(name="pin_code")
    private String pinCode;
    
    @Column(name="custom_fields", columnDefinition="json")
    private String customFields;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "updated_by")
    private Integer updatedBy;
    
    @Column(name = "is_active")
    private Boolean active;
}
