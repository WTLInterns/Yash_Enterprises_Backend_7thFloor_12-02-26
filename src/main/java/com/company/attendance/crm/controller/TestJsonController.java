package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.mapper.SimpleCrmMapper;
import com.company.attendance.crm.repository.BankRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestJsonController {

    private final BankRepository bankRepository;
    private final SimpleCrmMapper crmMapper;

    public TestJsonController(BankRepository bankRepository, SimpleCrmMapper crmMapper) {
        this.bankRepository = bankRepository;
        this.crmMapper = crmMapper;
    }

    @PostMapping("/bank")
    public ResponseEntity<String> testBankJson() {
        try {
            // Create a test bank with custom fields
            Bank bank = new Bank();
            bank.setId(null); // Auto-generated
            bank.setName("Test Bank JSON");
            bank.setCode("TEST001");
            bank.setActive(true);
            
            // Test JSON custom fields
            String customFields = "{\"customField1\":\"customValue1\",\"customField2\":123,\"customField3\":true}";
            
            bank.setCustomFields(customFields);
            
            // Save to database
            Bank saved = bankRepository.save(bank);
            
            // Convert to DTO
            var dto = crmMapper.toBankDto(saved);
            
            return ResponseEntity.ok("JSON Test Successful! Custom fields: " + dto.getCustomFields());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("JSON Test Failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/bank")
    public ResponseEntity<String> getTestBank() {
        try {
            return bankRepository.findAll().stream()
                .findFirst()
                .map(bank -> ResponseEntity.ok("Found bank: " + bank.getName() + ", Custom fields: " + bank.getCustomFields()))
                .orElse(ResponseEntity.ok("No banks found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Get Test Failed: " + e.getMessage());
        }
    }
}
