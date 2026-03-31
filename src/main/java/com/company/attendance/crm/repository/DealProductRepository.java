package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DealProductRepository extends JpaRepository<DealProduct, Long> {
    List<DealProduct> findByDeal(Deal deal);
    List<DealProduct> findByProduct(Product product);

    @Query("SELECT dp FROM DealProduct dp JOIN FETCH dp.product WHERE dp.deal.id = :dealId")
    List<DealProduct> findByDealIdWithProduct(@Param("dealId") Long dealId);
}
