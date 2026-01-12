package com.company.attendance.crm.mapper;

import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.dto.ClientDto;
import com.company.attendance.crm.dto.ProductDto;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.entity.Client;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CrmMapper {

    public BankDto toBankDto(Bank bank) {
        if (bank == null) return null;
        
        BankDto dto = new BankDto();
        dto.setId(bank.getId());
        dto.setName(bank.getName());
        dto.setBranchName(bank.getBranchName());
        dto.setPhone(bank.getPhone());
        dto.setWebsite(bank.getWebsite());
        dto.setAddress(bank.getAddress());
        dto.setDistrict(bank.getDistrict());
        dto.setTaluka(bank.getTaluka());
        dto.setPinCode(bank.getPinCode());
        dto.setDescription(bank.getDescription());
        dto.setCustomFields(bank.getCustomFields());
        dto.setActive(bank.getActive());
        
        // Audit fields
        dto.setCreatedAt(bank.getCreatedAt());
        dto.setUpdatedAt(bank.getUpdatedAt());
        dto.setCreatedBy(bank.getCreatedBy());
        dto.setUpdatedBy(bank.getUpdatedBy());
        
        return dto;
    }

    public ProductDto toProductDto(Product product) {
        if (product == null) return null;
        
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCode(product.getCode());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategoryId());
        dto.setPrice(product.getPrice());
        dto.setActive(product.getActive());
        
        // Audit fields
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());
        
        // Set owner name (in real app, fetch from user service)
        dto.setOwnerName("Admin User");
        dto.setCreatedByName("Admin User");
        dto.setUpdatedByName("Admin User");
        
        // Category name
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        
        // Custom fields
        if (product.getCustomFields() != null) {
            dto.setCustomFields(product.getCustomFields());
        } else {
            dto.setCustomFields("{}");
        }
        
        return dto;
    }

    public ClientDto toClientDto(Client client) {
        if (client == null) return null;
        
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setContactPhone(client.getContactPhone());
        dto.setAddress(client.getAddress());
        dto.setNotes(client.getNotes());
        dto.setActive(client.getIsActive());
        dto.setCustomFields(client.getCustomFields());
        
        // Audit fields - convert LocalDateTime to Instant
        if (client.getCreatedAt() != null) {
            dto.setCreatedAt(client.getCreatedAt().toInstant(java.time.ZoneOffset.UTC));
        }
        if (client.getUpdatedAt() != null) {
            dto.setUpdatedAt(client.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC));
        }
        dto.setCreatedBy(client.getCreatedBy() != null ? client.getCreatedBy().intValue() : null);
        dto.setUpdatedBy(client.getUpdatedBy() != null ? client.getUpdatedBy().intValue() : null);
        dto.setOwnerId(client.getOwnerId() != null ? client.getOwnerId().intValue() : null);
        
        // Set owner name (in real app, fetch from user service)
        dto.setOwnerName("Admin User");
        dto.setCreatedByName("Admin User");
        dto.setUpdatedByName("Admin User");
        
        return dto;
    }

    public Bank toBankEntity(BankDto dto) {
        if (dto == null) return null;
        
        Bank bank = new Bank();
        bank.setId(dto.getId());
        bank.setName(dto.getName());
        bank.setBranchName(dto.getBranchName());
        bank.setPhone(dto.getPhone());
        bank.setWebsite(dto.getWebsite());
        bank.setAddress(dto.getAddress());
        bank.setDistrict(dto.getDistrict());
        bank.setTaluka(dto.getTaluka());
        bank.setPinCode(dto.getPinCode());
        bank.setDescription(dto.getDescription());
        bank.setCustomFields(dto.getCustomFields());
        bank.setActive(dto.getActive());
        
        return bank;
    }

    public Product toProductEntity(ProductDto dto) {
        if (dto == null) return null;
        
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setCode(dto.getCode());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setActive(dto.getActive());
        
        // Custom fields
        if (dto.getCustomFields() != null) {
            product.setCustomFields(dto.getCustomFields());
        }
        
        return product;
    }

    public Client toClientEntity(ClientDto dto) {
        if (dto == null) return null;
        
        Client client = new Client();
        client.setId(dto.getId());
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setContactPhone(dto.getContactPhone());
        client.setAddress(dto.getAddress());
        client.setNotes(dto.getNotes());
        client.setIsActive(dto.getActive());
        
        return client;
    }
}
