package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Bank;
import org.springframework.data.jpa.domain.Specification;

public class BankSpecifications {
    public static Specification<Bank> active(Boolean active){
        return (root, q, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }
    public static Specification<Bank> owner(Long ownerId){
        return (root, q, cb) -> ownerId == null ? cb.conjunction() : cb.equal(root.get("createdBy"), ownerId);
    }
    public static Specification<Bank> q(String term){
        return (root, q, cb) -> term == null || term.isBlank() ? cb.conjunction() : cb.or(
                cb.like(cb.lower(root.get("name")), "%"+term.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("address")), "%"+term.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("city")), "%"+term.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("state")), "%"+term.toLowerCase()+"%")
        );
    }
}
