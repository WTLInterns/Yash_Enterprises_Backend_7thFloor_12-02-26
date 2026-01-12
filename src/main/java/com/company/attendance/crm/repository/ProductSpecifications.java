package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecifications {
    public static Specification<Product> active(Boolean active){
        return (root, q, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }
    public static Specification<Product> category(String category){
        return (root, q, cb) -> category == null || category.isBlank() ? cb.conjunction() : cb.equal(root.get("productCategory"), category);
    }
    public static Specification<Product> owner(Integer ownerId){
        return (root, q, cb) -> ownerId == null ? cb.conjunction() : cb.equal(root.get("ownerId"), ownerId);
    }
    public static Specification<Product> nameOrCodeContains(String term){
        return (root, q, cb) -> term == null || term.isBlank() ? cb.conjunction() : cb.or(
                cb.like(cb.lower(root.get("productName")), "%"+term.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("productCode")), "%"+term.toLowerCase()+"%")
        );
    }
    public static Specification<Product> categoryId(Integer categoryId){
        return (root, q, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("categoryId"), categoryId);
    }
}
