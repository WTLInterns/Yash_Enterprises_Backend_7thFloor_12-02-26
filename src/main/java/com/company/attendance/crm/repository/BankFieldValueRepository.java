package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.BankFieldDefinition;
import com.company.attendance.crm.entity.BankFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankFieldValueRepository extends JpaRepository<BankFieldValue, Long> {
    List<BankFieldValue> findByBank(Bank bank);
    Optional<BankFieldValue> findByBankAndFieldDefinition(Bank bank, BankFieldDefinition def);
}
