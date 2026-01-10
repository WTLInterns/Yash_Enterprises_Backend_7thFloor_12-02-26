package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.entity.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, UUID> {
    List<ProductPriceHistory> findByProductOrderByChangedAtDesc(Product product);
}
