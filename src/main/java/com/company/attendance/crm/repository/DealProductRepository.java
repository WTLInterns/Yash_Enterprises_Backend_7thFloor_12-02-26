package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DealProductRepository extends JpaRepository<DealProduct, UUID> {
    List<DealProduct> findByDeal(Deal deal);
    List<DealProduct> findByProduct(Product product);

    @Query(value = "select * from deal_products where substring(deal_id,1,16) = uuid_to_bin(:dealId)", nativeQuery = true)
    List<DealProduct> findByDealIdCompat(@Param("dealId") String dealId);
}
