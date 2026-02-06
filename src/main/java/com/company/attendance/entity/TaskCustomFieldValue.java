package com.company.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_custom_field_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCustomFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private TaskCustomField field;

    @Column(columnDefinition = "TEXT")
    private String value;
}
