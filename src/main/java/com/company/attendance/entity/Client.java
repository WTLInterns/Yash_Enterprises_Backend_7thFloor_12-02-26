package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String contactPhone;
    private String address;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    public boolean isActive() {
        return isActive != null && isActive;
    }
    
    @Column(columnDefinition = "json")
    private Map<String, Object> customFields;
    
    @Column(name = "owner_id")
    private UUID ownerId;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Case> cases;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private UUID createdBy;
    private UUID updatedBy;
}
