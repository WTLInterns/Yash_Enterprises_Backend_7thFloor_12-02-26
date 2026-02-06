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
    
    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
