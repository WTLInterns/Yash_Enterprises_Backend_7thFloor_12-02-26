package com.company.attendance.crm.mapper;

import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.entity.Bank;
import org.springframework.stereotype.Component;

@Component
public class SimpleCrmMapper {

    public Bank toBankEntity(BankDto dto) {
        if (dto == null) return null;
        
        Bank bank = new Bank();
        bank.setName(dto.getName());
        bank.setBranchName(dto.getBranchName());
        bank.setPhone(dto.getPhone());
        bank.setWebsite(dto.getWebsite());
        bank.setAddress(dto.getAddress());
        bank.setDescription(dto.getDescription());
        bank.setActive(dto.getActive());
        return bank;
    }

    public BankDto toBankDto(Bank bank) {
        if (bank == null) return null;
        
        BankDto dto = new BankDto();
        dto.setId(bank.getId());
        dto.setName(bank.getName());
        dto.setBranchName(bank.getBranchName());
        dto.setPhone(bank.getPhone());
        dto.setWebsite(bank.getWebsite());
        dto.setAddress(bank.getAddress());
        dto.setDescription(bank.getDescription());
        dto.setActive(bank.getActive());
        return dto;
    }
}
