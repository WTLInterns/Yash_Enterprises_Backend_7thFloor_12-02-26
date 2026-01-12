package com.company.attendance.crm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "deal_field_values", uniqueConstraints = @UniqueConstraint(name = "uk_deal_field_value", columnNames = {"deal_id", "field_definition_id"}))
public class DealFieldValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "field_definition_id", nullable = false)
    private DealFieldDefinition fieldDefinition;

    @Lob
    private String value;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }
    public DealFieldDefinition getFieldDefinition() { return fieldDefinition; }
    public void setFieldDefinition(DealFieldDefinition fieldDefinition) { this.fieldDefinition = fieldDefinition; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
