package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    private OffsetDateTime punchInTime;
    private OffsetDateTime punchOutTime;

    private BigDecimal inLocationLat;
    private BigDecimal inLocationLng;
    private BigDecimal outLocationLat;
    private BigDecimal outLocationLng;

    // Derived fields
    @Column(precision = 10, scale = 2)
    private BigDecimal totalHours;

    @Column(precision = 10, scale = 3)
    private BigDecimal totalKm;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String note;
    private String deviceInfo;
    private Boolean isLunchIn;
    private Boolean isLunchOut;

    public enum Status {
        // Punch lifecycle (legacy / internal)
        PUNCHED_IN, PUNCHED_OUT,

        // Attendance status (business)
        PRESENT, LATE, HALF_DAY, ABSENT, ON_LEAVE,

        // Dashboard / HR statuses
        PENDING, HOLIDAY, WEEKLY_OFF
    }

    // Explicit getter used by AttendanceService (defensive if Lombok is not processed)

    public Status getStatus() {
        return status;
    }
}
