package com.company.attendance.crm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bank_field_values", uniqueConstraints = @UniqueConstraint(name = "uk_bank_field_value", columnNames = {"bank_id","field_definition_id"}))
public class BankFieldValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "field_definition_id", nullable = false)
    private BankFieldDefinition fieldDefinition;

    @Lob
    private String value;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Bank getBank() { return bank; }
    public void setBank(Bank bank) { this.bank = bank; }
    public BankFieldDefinition getFieldDefinition() { return fieldDefinition; }
    public void setFieldDefinition(BankFieldDefinition fieldDefinition) { this.fieldDefinition = fieldDefinition; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
