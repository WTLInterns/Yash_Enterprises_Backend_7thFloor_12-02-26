package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.BankContactPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankContactPersonRepository extends JpaRepository<BankContactPerson, Long> {
    List<BankContactPerson> findByBankId(Long bankId);
    void deleteByBankId(Long bankId);
}
