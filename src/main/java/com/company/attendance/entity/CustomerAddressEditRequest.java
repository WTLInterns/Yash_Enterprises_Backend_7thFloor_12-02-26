package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_address_edit_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddressEditRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "address_id", nullable = false)
    private Long addressId;
    
    @Column(name = "requested_by_employee_id", nullable = false)
    private Long requestedByEmployeeId;
    
    // 🔥 Proposed new values
    @Column(name = "new_address_line", length = 500)
    private String newAddressLine;

    @Column(name = "new_city", length = 100)
    private String newCity;

    @Column(name = "new_state", length = 100)
    private String newState;

    @Column(name = "new_pincode", length = 20)
    private String newPincode;

    @Column(name = "new_country", length = 100)
    private String newCountry;

    @Column(name = "new_latitude")
    private Double newLatitude;

    @Column(name = "new_longitude")
    private Double newLongitude;

    @Column(name = "reason", length = 1000)
    private String reason;

    // 🔥 DEPARTMENT & TASK LINKING FIELDS
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "created_by_tl_id")
    private Long createdByTlId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
    
    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
