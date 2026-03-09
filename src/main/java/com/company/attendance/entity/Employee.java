package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    @Column(unique = true)
    private String employeeId;

    @Column(name = "full_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 32)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    private Long subadminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String profileImageUrl;

    @Column(length = 512)
    private String fcmTokenMobile;

    @Column(length = 512)
    private String fcmTokenWeb;

    private LocalDate hiredAt;

    private LocalDate terminationDate;

    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private Employee reportingManager;

    // ✅ TEAM LEAD: New field for TL assignment (separate from Reporting Manager)
    // Used when role = EMPLOYEE to assign department ownership
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tl_id")
    private Employee tl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // ✅ TL DEPARTMENT: Store department as string for TL roles (PPO, PPE, HLC, etc.)
    @Column(name = "department_name")
    private String departmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Team> teams = new HashSet<>();

    private BigDecimal locationLat;
    private BigDecimal locationLng;

    private String employeeCode;
    private Boolean attendanceAllowed;

    // Additional fields
    private String customDesignation;
    private LocalDate dateOfBirth;
    private String gender;

    // Auditing
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    // ✅ NEW: Helper method for full name
    public String getFullName() {
        if (lastName != null && !lastName.trim().isEmpty()) {
            return (firstName + " " + lastName).trim();
        }
        return firstName != null ? firstName.trim() : "";
    }

    public enum Status {
        ACTIVE, INACTIVE, TERMINATED
    }
}
