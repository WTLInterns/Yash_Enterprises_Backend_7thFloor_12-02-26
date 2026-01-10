package com.company.attendance.crm.mapper;

import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.dto.ClientDto;
import com.company.attendance.crm.dto.ProductDto;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.entity.Client;
import com.company.attendance.crm.service.BankService;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
public class CrmMapper {

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
        
        // Audit fields
        dto.setCreatedAt(bank.getCreatedAt() != null ? bank.getCreatedAt().toInstant() : null);
        dto.setUpdatedAt(bank.getUpdatedAt() != null ? bank.getUpdatedAt().toInstant() : null);
        dto.setCreatedBy(bank.getCreatedBy());
        dto.setUpdatedBy(bank.getUpdatedBy());
        dto.setOwnerId(bank.getOwnerId());
        
        // Custom fields - temporarily disabled
        dto.setCustomFields(new HashMap<>());
        
        return dto;
    }

    public ProductDto toProductDto(Product product) {
        if (product == null) return null;
        
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setProductCode(product.getProductCode());
        dto.setDescription(product.getDescription());
        dto.setProductCategory(product.getProductCategory());
        dto.setUnitPrice(product.getUnitPrice() != null ? product.getUnitPrice().doubleValue() : null);
        dto.setCategoryId(product.getCategoryId());
        dto.setActive(product.isActive());
        
        // Audit fields
        dto.setCreatedAt(product.getCreatedAt() != null ? product.getCreatedAt().toInstant() : null);
        dto.setUpdatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().toInstant() : null);
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());
        dto.setOwnerId(product.getOwnerId());
        
        // Custom fields - temporarily disabled
        dto.setCustomFields(new HashMap<>());
        
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
        
        // Audit fields
        dto.setCreatedAt(client.getCreatedAt() != null ? client.getCreatedAt().toInstant(ZoneOffset.UTC) : null);
        dto.setUpdatedAt(client.getUpdatedAt() != null ? client.getUpdatedAt().toInstant(ZoneOffset.UTC) : null);
        dto.setCreatedBy(client.getCreatedBy());
        dto.setUpdatedBy(client.getUpdatedBy());
        dto.setOwnerId(client.getOwnerId());
        
        // Custom fields - temporarily disabled
        dto.setCustomFields(new HashMap<>());
        
        return dto;
    }

    public Bank toBankEntity(BankDto dto) {
        if (dto == null) return null;
        
        Bank bank = new Bank();
        bank.setId(dto.getId());
        bank.setBankName(dto.getBankName());
        bank.setBranchName(dto.getBranchName());
        bank.setAddress(dto.getAddress());
        bank.setPhone(dto.getPhone());
        bank.setWebsite(dto.getWebsite());
        bank.setDistrict(dto.getDistrict());
        bank.setTaluka(dto.getTaluka());
        bank.setPinCode(dto.getPinCode());
        bank.setDescription(dto.getDescription());
        bank.setActive(dto.isActive());
        bank.setOwnerId(dto.getOwnerId());
        
        // Custom fields
        if (dto.getCustomFields() != null) {
            bank.setCustomFields(dto.getCustomFields());
        }
        
        return bank;
    }

    public Product toProductEntity(ProductDto dto) {
        if (dto == null) return null;
        
        Product product = new Product();
        product.setId(dto.getId());
        product.setProductName(dto.getProductName());
        product.setProductCode(dto.getProductCode());
        product.setDescription(dto.getDescription());
        product.setProductCategory(dto.getProductCategory());
        product.setUnitPrice(dto.getUnitPrice() != null ? java.math.BigDecimal.valueOf(dto.getUnitPrice()) : null);
        product.setCategoryId(dto.getCategoryId());
        product.setActive(dto.isActive());
        product.setOwnerId(dto.getOwnerId());
        
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
        client.setIsActive(dto.isActive());
        client.setOwnerId(dto.getOwnerId());
        
        // Custom fields
        if (dto.getCustomFields() != null) {
            client.setCustomFields(dto.getCustomFields());
        }
        
        return client;
    }
}
