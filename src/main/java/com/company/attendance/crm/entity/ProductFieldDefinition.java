package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_field_definitions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_field_key", columnNames = {"field_key"})
})
public class ProductFieldDefinition {
    @Id
    @Column(length = 36)
    private UUID id;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "field_key", nullable = false, unique = true)
    private String fieldKey;

    @Column(name = "field_type", nullable = false)
    private String fieldType; // TEXT, NUMBER, DATE, BOOLEAN, DROPDOWN

    @Column(name = "required")
    private Boolean required = false;

    @Column(name = "active")
    private Boolean active = true;

    @PrePersist
    public void prePersist(){ if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
