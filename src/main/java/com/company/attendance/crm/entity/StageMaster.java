package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "stage_master")
@Data
public class StageMaster {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "department", nullable = false)
    private String department;   // PPE, PPO, PSD, HLC, ROP, ACCOUNT
    
    @Column(name = "stage_code", nullable = false)
    private String stageCode;    // NEW_LEAD, DOC_COLLECT, PDO, etc
    
    @Column(name = "stage_name", nullable = false)
    private String stageName;    // Display name: "New Lead", "Doc Collect"
    
    @Column(name = "stage_order")
    private Integer stageOrder;  // Sequence for ordering
    
    @Column(name = "is_terminal")
    private Boolean isTerminal = false;  // Close Win / Close Lost
}
