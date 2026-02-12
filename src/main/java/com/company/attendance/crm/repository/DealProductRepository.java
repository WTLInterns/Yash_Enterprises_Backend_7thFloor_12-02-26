package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealProductRepository extends JpaRepository<DealProduct, Long> {
    List<DealProduct> findByDeal(Deal deal);
    List<DealProduct> findByProduct(Product product);
}
