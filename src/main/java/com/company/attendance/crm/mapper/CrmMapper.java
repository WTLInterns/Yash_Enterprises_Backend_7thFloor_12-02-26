package com.company.attendance.crm.mapper;

import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.dto.ClientDto;
import com.company.attendance.crm.dto.DealDto;
import com.company.attendance.crm.dto.ProductDto;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.entity.ProductCategory;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.ClientRepository;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Optional;

@Component
public class CrmMapper {

  private final EmployeeRepository employeeRepository;
  private final ClientRepository clientRepository;

  public CrmMapper(EmployeeRepository employeeRepository, ClientRepository clientRepository) {
    this.employeeRepository = employeeRepository;
    this.clientRepository = clientRepository;
  }

  private String employeeName(Long employeeId) {
    if (employeeId == null) return null;
    Optional<Employee> emp = employeeRepository.findById(employeeId);
    if (emp.isEmpty()) return null;
    String first = emp.get().getFirstName() != null ? emp.get().getFirstName().trim() : "";
    String last = emp.get().getLastName() != null ? emp.get().getLastName().trim() : "";
    String full = (first + " " + last).trim();
    return full.isEmpty() ? null : full;
  }

  // ---- Bank ----
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
    dto.setCreatedAt(bank.getCreatedAt());
    dto.setUpdatedAt(bank.getUpdatedAt());
    dto.setCreatedBy(bank.getCreatedBy());
    dto.setUpdatedBy(bank.getUpdatedBy());
    dto.setCreatedByName(employeeName(bank.getCreatedBy()));
    dto.setUpdatedByName(employeeName(bank.getUpdatedBy()));
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

  // ---- Product ----
  public ProductDto toProductDto(Product product) {
    if (product == null) return null;
    ProductDto dto = new ProductDto();
    dto.setId(product.getId());
    dto.setName(product.getName());
    dto.setCode(product.getCode());
    dto.setDescription(product.getDescription());
    dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
    dto.setPrice(product.getPrice());
    dto.setActive(product.getActive());
    dto.setCreatedAt(product.getCreatedAt());
    dto.setUpdatedAt(product.getUpdatedAt());
    dto.setCreatedBy(product.getCreatedBy());
    dto.setUpdatedBy(product.getUpdatedBy());
    dto.setCreatedByName(employeeName(product.getCreatedBy()));
    dto.setUpdatedByName(employeeName(product.getUpdatedBy()));
    dto.setOwnerName(employeeName(product.getCreatedBy()));
    dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
    dto.setCustomFields(product.getCustomFields() != null ? product.getCustomFields() : "{}");
    return dto;
  }

  public Product toProductEntity(ProductDto dto) {
    if (dto == null) return null;
    Product product = new Product();
    product.setId(dto.getId());
    product.setName(dto.getName());
    product.setCode(dto.getCode());
    product.setDescription(dto.getDescription());
    // Set category relationship if categoryId is provided
    if (dto.getCategoryId() != null) {
        ProductCategory category = new ProductCategory();
        category.setId(dto.getCategoryId());
        product.setCategory(category);
    }
    product.setPrice(dto.getPrice());
    product.setActive(dto.getActive());
    if (dto.getCustomFields() != null) {
      product.setCustomFields(dto.getCustomFields());
    }
    return product;
  }

  // ---- Client ----
  public ClientDto toClientDto(Client client) {
    if (client == null) return null;
    ClientDto dto = new ClientDto();
    dto.setId(client.getId());
    dto.setName(client.getName());
    dto.setEmail(client.getEmail());
    dto.setContactPhone(client.getContactPhone());
    dto.setAddress(client.getAddress());
    
    // Geocoding fields
    dto.setLatitude(client.getLatitude());
    dto.setLongitude(client.getLongitude());
    dto.setCity(client.getCity());
    dto.setPincode(client.getPincode());
    dto.setState(client.getState());
    dto.setCountry(client.getCountry());
    
    // Contact details
    dto.setContactName(client.getContactName());
    dto.setContactNumber(client.getContactNumber());
    dto.setCountryCode(client.getCountryCode());
    
    dto.setNotes(client.getNotes());
    dto.setActive(client.getIsActive());

    if (client.getCreatedAt() != null) {
      dto.setCreatedAt(client.getCreatedAt().toInstant(ZoneOffset.UTC));
    }
    if (client.getUpdatedAt() != null) {
      dto.setUpdatedAt(client.getUpdatedAt().toInstant(ZoneOffset.UTC));
    }
    dto.setCreatedBy(client.getCreatedBy());
    dto.setUpdatedBy(client.getUpdatedBy());
    dto.setOwnerId(client.getOwnerId());

    dto.setCreatedByName(employeeName(client.getCreatedBy()));
    dto.setUpdatedByName(employeeName(client.getUpdatedBy()));
    dto.setOwnerName(employeeName(client.getOwnerId()));

    return dto;
}

public Client toClientEntity(ClientDto dto) {
    if (dto == null) return null;
    Client client = new Client();
    client.setId(dto.getId());
    client.setName(dto.getName());
    client.setEmail(dto.getEmail());
    client.setContactPhone(dto.getContactPhone());
    client.setAddress(dto.getAddress());
    
    // Geocoding fields
    client.setLatitude(dto.getLatitude());
    client.setLongitude(dto.getLongitude());
    client.setCity(dto.getCity());
    client.setPincode(dto.getPincode());
    client.setState(dto.getState());
    client.setCountry(dto.getCountry());
    
    // Contact details
    client.setContactName(dto.getContactName());
    client.setContactNumber(dto.getContactNumber());
    client.setCountryCode(dto.getCountryCode());
    
    client.setNotes(dto.getNotes());
    client.setIsActive(dto.getActive());
    client.setCustomFields(dto.getCustomFields());
    client.setOwnerId(dto.getOwnerId());
    client.setCreatedBy(dto.getCreatedBy());
    client.setUpdatedBy(dto.getUpdatedBy());
    
    return client;
  }

  // ---- Deal (FIXED) ----
  public DealDto toDealDto(Deal deal) {
    if (deal == null) return null;

    DealDto dto = new DealDto();
    dto.setId(deal.getId());
    dto.setClientId(deal.getClientId());
    dto.setBankId(deal.getBankId()); // IMPORTANT: prefill bank dropdown on UI

    dto.setName(deal.getName());
    dto.setValueAmount(deal.getValueAmount());
    dto.setClosingDate(deal.getClosingDate());
    dto.setBranchName(deal.getBranchName());
    dto.setRelatedBankName(deal.getRelatedBankName());
    dto.setDescription(deal.getDescription());
    dto.setRequiredAmount(deal.getRequiredAmount());
    dto.setOutstandingAmount(deal.getOutstandingAmount());
    dto.setStage(deal.getStage() != null ? deal.getStage().name() : null);
    dto.setActive(deal.getActive());

    if (deal.getCreatedAt() != null) {
      dto.setCreatedAt(deal.getCreatedAt().toInstant(ZoneOffset.UTC));
    }
    if (deal.getUpdatedAt() != null) {
      dto.setUpdatedAt(deal.getUpdatedAt().toInstant(ZoneOffset.UTC));
    }
    dto.setCreatedBy(deal.getCreatedBy());
    dto.setUpdatedBy(deal.getUpdatedBy());
    dto.setCreatedByName(employeeName(deal.getCreatedBy()));
    dto.setUpdatedByName(employeeName(deal.getUpdatedBy()));
    
    // Owner should come from the Client, not the Deal
    Client client = deal.getClient();
    if (client == null && deal.getClientId() != null) {
      client = clientRepository.findById(deal.getClientId()).orElse(null);
    }
    
    if (client != null && client.getOwnerId() != null) {
      dto.setOwnerName(employeeName(client.getOwnerId()));
      dto.setClientName(client.getName());
    } else {
      dto.setOwnerName(null);
      dto.setClientName(null);
    }

    // Optional display fields if relations loaded
    dto.setBankName(deal.getBank() != null ? deal.getBank().getName() : null);

    // Keep customFields default if absent
    if (dto.getCustomFields() == null) {
      dto.setCustomFields("{}");
    }
    return dto;
  }

  public Deal toDealEntity(DealDto dto) {
    if (dto == null) return null;

    Deal deal = new Deal();
    deal.setId(dto.getId());
    deal.setClientId(dto.getClientId());
    deal.setBankId(dto.getBankId()); // IMPORTANT: persist bankId from UI
    deal.setName(dto.getName());
    deal.setValueAmount(dto.getValueAmount());
    deal.setClosingDate(dto.getClosingDate());
    deal.setBranchName(dto.getBranchName());
    deal.setRelatedBankName(dto.getRelatedBankName());
    deal.setDescription(dto.getDescription());
    deal.setRequiredAmount(dto.getRequiredAmount());
    deal.setOutstandingAmount(dto.getOutstandingAmount());
    if (dto.getStage() != null) {
      try {
        deal.setStage(com.company.attendance.crm.enums.DealStage.valueOf(dto.getStage()));
      } catch (IllegalArgumentException ignored) {}
    }
    deal.setActive(dto.getActive());
    return deal;
  }
}