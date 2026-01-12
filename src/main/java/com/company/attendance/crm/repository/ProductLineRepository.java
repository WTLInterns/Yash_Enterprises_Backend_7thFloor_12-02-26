package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ProductLine;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductLineRepository extends JpaRepository<ProductLine, Integer> {
    List<ProductLine> findByDeal(Deal deal);
}
