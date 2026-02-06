package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String contactPhone;
    private String address;
    
    // ✅ NEW: Geocoding fields
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "pincode")
    private String pincode;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "country")
    private String country;
    
    // ✅ NEW: Contact details
    @Column(name = "contact_name")
    private String contactName;
    
    @Column(name = "contact_number")
    private String contactNumber;
    
    @Column(name = "country_code")
    private String countryCode;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name="custom_fields", columnDefinition="json")
    private String customFields;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    private Long updatedBy;
    private Long ownerId;
}
