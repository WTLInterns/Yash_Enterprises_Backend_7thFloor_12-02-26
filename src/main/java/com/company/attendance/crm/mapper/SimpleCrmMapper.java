package com.company.attendance.crm.mapper;

import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.entity.Bank;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class SimpleCrmMapper {

    public BankDto toBankDto(Bank bank) {
        if (bank == null) return null;
        
        BankDto dto = new BankDto();
        dto.setId(bank.getId());
        dto.setBankName(bank.getBankName());
        dto.setBranchName(bank.getBranchName());
        dto.setAddress(bank.getAddress());
        dto.setPhone(bank.getPhone());
        dto.setWebsite(bank.getWebsite());
        dto.setDistrict(bank.getDistrict());
        dto.setTaluka(bank.getTaluka());
        dto.setPinCode(bank.getPinCode());
        dto.setDescription(bank.getDescription());
        dto.setActive(bank.isActive());
        
        // Test JSON mapping - this should work now
        if (bank.getCustomFields() != null) {
            dto.setCustomFields(bank.getCustomFields());
            System.out.println("JSON MAPPING SUCCESS: " + bank.getCustomFields());
        } else {
            dto.setCustomFields(new HashMap<>());
            System.out.println("JSON MAPPING: No custom fields, using empty map");
        }
        
        return dto;
    }
}
