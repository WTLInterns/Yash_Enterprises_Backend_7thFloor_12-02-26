package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "activity_field_values", uniqueConstraints = @UniqueConstraint(name = "uk_activity_field_value", columnNames = {"activity_id","field_definition_id"}))
public class ActivityFieldValue {
    @Id @Column(length = 36) private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "field_definition_id", nullable = false)
    private ActivityFieldDefinition fieldDefinition;

    @Lob
    private String value;

    @PrePersist
    public void prePersist(){ if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }
    public ActivityFieldDefinition getFieldDefinition() { return fieldDefinition; }
    public void setFieldDefinition(ActivityFieldDefinition fieldDefinition) { this.fieldDefinition = fieldDefinition; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
