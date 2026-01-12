package com.company.attendance.crm.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_category_name", columnNames = {"name"})
})
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private Boolean active = true;

    @PrePersist
    public void prePersist(){ if (active == null) active = true; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
