package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Bank;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class BankSpecifications {
    public static Specification<Bank> active(Boolean active){
        return (root, q, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }
    public static Specification<Bank> owner(UUID ownerId){
        return (root, q, cb) -> ownerId == null ? cb.conjunction() : cb.equal(root.get("ownerId"), ownerId);
    }
    public static Specification<Bank> q(String term){
        return (root, q, cb) -> term == null || term.isBlank() ? cb.conjunction() : cb.or(
                cb.like(cb.lower(root.get("bankName")), "%"+term.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("branchName")), "%"+term.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("district")), "%"+term.toLowerCase()+"%")
        );
    }
}
