package com.company.attendance.entity;

import com.company.attendance.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Task main
    @Column(nullable = false)
    private String taskName;

    private String taskDescription;

    // Task Type
    private String customTaskType;   // "Default Task", "Collect Payment", etc.

    // Assignment
    @Column(name = "assigned_to_employee_id")
    private Long assignedToEmployeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_employee_id", insertable = false, updatable = false)
    private Employee assignedToEmployee;

    @Column(name = "created_by_employee_id")
    private Long createdByEmployeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_employee_id", insertable = false, updatable = false)
    private Employee createdByEmployee;

    // Scheduling - Date fields
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;

    private Boolean repeatTask;

    // Status as ENUM
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    private String completion; // Completed / Pending / Delayed

    // Relations
    private String taskAgainst; // CLIENT or ROUTE (only one)

    @Column(name = "client_id")
    private Long clientId;

    // ✅ FIXED: Client entity should be from attendance.entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    private Long routeId;

    // Address related
    private String address;

    @Column(name = "customer_address_id")
    private Long customerAddressId;

    // Metadata
    private String internalTaskId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dynamic Custom Field Values
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskCustomFieldValue> customFieldValues = new ArrayList<>();
}
