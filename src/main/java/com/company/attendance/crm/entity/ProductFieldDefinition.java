package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "product_field_definitions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_field_key", columnNames = {"field_key"})
})
public class ProductFieldDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "field_key", nullable = false, unique = true)
    private String fieldKey;

    @Column(name = "field_type", nullable = false)
    private String fieldType; // TEXT, NUMBER, DATE, BOOLEAN, DROPDOWN

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json")
    private List<String> optionsJson;

    @Column(name = "required")
    private Boolean required = false;

    @Column(name = "active")
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public List<String> getOptionsJson() { return optionsJson; }
    public void setOptionsJson(List<String> optionsJson) { this.optionsJson = optionsJson; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
