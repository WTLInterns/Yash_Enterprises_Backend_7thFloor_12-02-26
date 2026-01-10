package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.ProductLine;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductLineRepository extends JpaRepository<ProductLine, UUID> {
    List<ProductLine> findByDeal(Deal deal);
}
