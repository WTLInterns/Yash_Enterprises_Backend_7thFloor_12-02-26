package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_field_values", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_field_value_unique", columnNames = {"product_id","field_definition_id"})
})
public class ProductFieldValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "field_definition_id", nullable = false)
    private ProductFieldDefinition fieldDefinition;

    @Lob
    private String value; // store as string/JSON

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public ProductFieldDefinition getFieldDefinition() { return fieldDefinition; }
    public void setFieldDefinition(ProductFieldDefinition fieldDefinition) { this.fieldDefinition = fieldDefinition; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
