package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealFieldDefinition;
import com.company.attendance.crm.entity.DealFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealFieldValueRepository extends JpaRepository<DealFieldValue, Long> {
    List<DealFieldValue> findByDeal(Deal deal);
    Optional<DealFieldValue> findByDealAndFieldDefinition(Deal deal, DealFieldDefinition def);
}
