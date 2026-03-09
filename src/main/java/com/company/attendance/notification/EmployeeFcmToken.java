package com.company.attendance.notification;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_fcm_tokens", 
       indexes = @Index(name = "idx_employee_token", columnList = "employee_id,token"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFcmToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "token", nullable = false, length = 500)
    private String token;
    
    @Column(name = "platform", nullable = false)
    private String platform; // "WEB", "MOBILE"
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Optional: Device identifier for better management
    @Column(name = "device_id", length = 200)
    private String deviceId;
    
    // Pre-persist method to set created_at
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
