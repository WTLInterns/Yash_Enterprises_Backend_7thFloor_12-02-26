package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Product> findByNameIgnoreCase(String name);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.active = true")
    Page<Product> findByActiveTrue(Pageable pageable);
}
