package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Display name of the site. This maps to the frontend field `siteName`.
     */
    @Column(name = "site_name", nullable = false, length = 150)
    private String siteName;

    @Column(name = "site_id", nullable = false, length = 100)
    private String siteId;

    @Column(length = 500)
    private String address;

    @Column(length = 200)
    private String email;

    @Column(length = 1000)
    private String description;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "contact_number", length = 50)
    private String contactNumber;

    private Double latitude;

    private Double longitude;

    @Column(length = 150)
    private String city;

    @Column(length = 20)
    private String pincode;

    // ✅ NEW: Client mapping for CRM structure
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
