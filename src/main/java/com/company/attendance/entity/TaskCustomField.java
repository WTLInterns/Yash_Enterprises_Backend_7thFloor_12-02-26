package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_custom_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCustomField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fieldKey;        // "amountSpent"
    private String fieldLabel;      // "How much amount have you spend?"
    private String fieldType;       // TEXT, NUMBER, SELECT, PHOTO
    private Boolean required;
    private Boolean active;

    // optional: show for which task type
    private String customTaskType;  // Default Task etc
    
    // ✅ Added sortOrder for ordering fields
    private Integer sortOrder;
}
