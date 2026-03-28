package com.company.attendance.crm.service;

import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.dto.DealDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.StageMaster;
import com.company.attendance.crm.repository.BankRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.ProductRepository;
import com.company.attendance.crm.repository.DealProductRepository;
import com.company.attendance.crm.repository.StageMasterRepository;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.entity.Employee;
import com.company.attendance.service.ClientService;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.EmployeeRepository;

import java.util.ArrayList; // 🔥 FIX: Import ArrayList for stage history
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealExcelImportService {

    private final DealService dealService;
    private final BankService bankService;
    private final ClientService clientService;
    private final BankRepository bankRepository;
    private final DealRepository dealRepository;
    private final ProductRepository productRepository;
    private final DealProductRepository dealProductRepository;
    private final ClientRepository clientRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final EmployeeRepository employeeRepository;
    private final CrmMapper crmMapper;
    private final StageMasterRepository stageMasterRepository;

    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "Customer Name", "Product", "Stage"
    );

    // 🔥 FIX: Remove @Transactional from main method to prevent full rollback on single row failure
    public Map<String, Object> importDealsFromExcel(
            org.springframework.web.multipart.MultipartFile file,
            String userDepartment,
            boolean allowDepartmentOverride,
            Long ownerUserId) throws Exception {
        
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Validate headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel file has no headers");
            }
            
            Map<String, Integer> headerMap = validateHeaders(headerRow);
            
            // Per-department count cache — avoids one DB call per row
            Map<String, Long> deptCountCache = new HashMap<>();
            
            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                
                totalRows++;
                
                try {
                    importSingleRow(row, headerMap, userDepartment, allowDepartmentOverride, deptCountCache, ownerUserId);
                    successCount++;
                } catch (Exception e) {
                    String error = String.format("Row %d: %s", i + 1, e.getMessage());
                    errors.add(error);
                    log.error("❌ FULL ERROR ROW {} ", i + 1, e); // 🔥 FIX: Full error logging
                }
            }
        }
        
        result.put("totalRows", totalRows);
        result.put("success", successCount);
        result.put("failed", totalRows - successCount);
        result.put("errors", errors);
        
        return result;
    }

    private Map<String, Integer> validateHeaders(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        
        for (Cell cell : headerRow) {
            String header = getCellValueAsString(cell).trim();
            if (!header.isEmpty()) {
                headerMap.put(normalizeHeader(header), cell.getColumnIndex());
            }
        }
        
        // Check required headers with variations
        for (String required : REQUIRED_HEADERS) {
            if (!hasRequiredHeader(headerMap, required)) {
                throw new RuntimeException("Missing required header: " + required + 
                    " (found: " + String.join(", ", headerMap.keySet()) + ")");
            }
        }
        
        return headerMap;
    }
    
    /**
     * Normalize header name for flexible matching
     */
    private String normalizeHeader(String header) {
        return header.trim().toLowerCase().replaceAll("\\s+", "");
    }
    
    /**
     * Check if required header exists (allows variations)
     */
    private boolean hasRequiredHeader(Map<String, Integer> headerMap, String requiredHeader) {
        String normalizedRequired = normalizeHeader(requiredHeader);
        
        // Direct match
        if (headerMap.containsKey(normalizedRequired)) {
            return true;
        }
        
        // Check common variations
        return switch (normalizedRequired) {
            case "district" -> headerMap.containsKey("district") || headerMap.containsKey("dist");
            case "customernumber" -> headerMap.containsKey("customernumber") || headerMap.containsKey("customerno");
            case "village" -> headerMap.containsKey("village") || headerMap.containsKey("address");
            case "taluka" -> headerMap.containsKey("taluka") || headerMap.containsKey("tehsil");
            default -> headerMap.containsKey(normalizedRequired);
        };
    }

    /**
     * Resolve Excel stage value to a valid stageCode from stage_master.
     * Tries: exact stageCode match → stageName match → normalizeStage fallback.
     */
    private String resolveStageCode(String excelStage, String department) {
        if (excelStage == null || excelStage.trim().isEmpty()) return "NEW_LEAD";

        List<StageMaster> stages = stageMasterRepository.findByDepartmentOrderByStageOrder(department);

        // 1. Exact stageCode match (case-insensitive)
        String upper = excelStage.trim().toUpperCase();
        for (StageMaster s : stages) {
            if (s.getStageCode().equalsIgnoreCase(upper)) return s.getStageCode();
        }

        // 2. stageName match (case-insensitive, ignore spaces/underscores)
        String normalized = upper.replace(" ", "").replace("_", "");
        for (StageMaster s : stages) {
            String nameNorm = s.getStageName().toUpperCase().replace(" ", "").replace("_", "");
            String codeNorm = s.getStageCode().toUpperCase().replace("_", "");
            if (nameNorm.equals(normalized) || codeNorm.equals(normalized)) return s.getStageCode();
        }

        // 3. Fallback: use normalizeStage (may not be in stage_master but saves the deal)
        String fallback = normalizeStage(excelStage);
        log.warn("Stage '{}' not found in stage_master for dept '{}', using fallback: {}", excelStage, department, fallback);
        return fallback;
    }

    /**
     * Normalize stage names to match database format
     */
    private String normalizeStage(String stage) {
        if (stage == null || stage.trim().isEmpty()) return "NEW_LEAD";

        String s = stage.trim().toUpperCase().replace(" ", "_");

        return switch (s) {
            case "DOC_COLLECT", "DOCUMENT_COLLECTION", "DOC.COLLECT" -> "DOC_COLLECT";
            case "NEW_LEAD", "LEAD", "NEW_LEADS" -> "NEW_LEAD";
            case "DOP" -> "DOP";
            case "ACCOUNT" -> "ACCOUNT";
            case "CLOSE_WIN", "CLOSED_WON", "CLOSE_WIN_", "CLOSEWIN" -> "CLOSE_WIN";
            case "CLOSE_LOST", "CLOSED_LOST", "CLOSE_LOST_", "CLOSELOST" -> "CLOSE_LOST";
            case "LOAN_APPLICATION", "LOAN_APP" -> "LOAN_APPLICATION";
            case "BILLING" -> "BILLING";
            case "PENDING" -> "PENDING";
            case "IN_PROCESS", "INPROCESS" -> "IN_PROCESS";
            case "HOLD" -> "HOLD";
            case "PHYSICAL_POSSESSION" -> "PHYSICAL_POSSESSION";
            case "DMO", "DM_ORDER", "DM_ORDER_" -> "DMO";
            case "CJMO" -> "CJMO";
            case "RECOVERY" -> "RECOVERY";
            default -> s; // keep as-is (already uppercased+underscored)
        };
    }

    // @Transactional removed — private methods are not proxied by Spring AOP
    private void importSingleRow(Row row, Map<String, Integer> headerMap,
                                String userDepartment, boolean allowDepartmentOverride,
                                Map<String, Long> deptCountCache, Long ownerUserId) throws Exception {
        
        // Get header indices
        Integer customerNameIndex = findHeaderIndex(headerMap, "Customer Name");
        Integer villageIndex = findHeaderIndex(headerMap, "Village");
        Integer talukaIndex = findHeaderIndex(headerMap, "Taluka");
        Integer districtIndex = findHeaderIndex(headerMap, "District");
        Integer productNameIndex = findHeaderIndex(headerMap, "Product");
        Integer departmentIndex = findHeaderIndex(headerMap, "Department");
        Integer stageIndex = findHeaderIndex(headerMap, "Stage");
        Integer appNoIndex = findHeaderIndex(headerMap, "App No");
        Integer bankNameIndex = findHeaderIndex(headerMap, "Bank Name");
        Integer branchNameIndex = findHeaderIndex(headerMap, "Branch Name");
        Integer contactNameIndex = findHeaderIndex(headerMap, "Contact Name");
        Integer allotmentLetterIndex = findHeaderIndex(headerMap, "Allotment Letter");
        Integer closingDateIndex = findHeaderIndex(headerMap, "Closing Date");
        Integer allocationDateIndex = findHeaderIndex(headerMap, "Allocation Date");
        Integer amountIndex = findHeaderIndex(headerMap, "Amount");
        Integer addressTypeIndex = findHeaderIndex(headerMap, "Address Type");
        Integer accountStatusIndex = findHeaderIndex(headerMap, "Account Status");
        
        // Extract values safely (never null, returns "-" for empty)
        String customerName = customerNameIndex != null ? getCellValueSafely(row, customerNameIndex) : null;
        String village = villageIndex != null ? getCellValueSafely(row, villageIndex) : null;
        String taluka = talukaIndex != null ? getCellValueSafely(row, talukaIndex) : null;
        String district = districtIndex != null ? getCellValueSafely(row, districtIndex) : null;
        String productName = productNameIndex != null ? getCellValueSafely(row, productNameIndex) : null;
        String department = departmentIndex != null ? getCellValueSafely(row, departmentIndex) : null;
        String stage = stageIndex != null ? getCellValueSafely(row, stageIndex) : null;
        
        // Optional fields
        String appNo = appNoIndex != null ? getCellValueSafely(row, appNoIndex) : null;
        String bankName = bankNameIndex != null ? getCellValueSafely(row, bankNameIndex) : null;
        String branchName = branchNameIndex != null ? getCellValueSafely(row, branchNameIndex) : null;
        String contactName = contactNameIndex != null ? getCellValueSafely(row, contactNameIndex) : null;
        String allotmentLetter = allotmentLetterIndex != null ? getCellValueSafely(row, allotmentLetterIndex) : null;
        LocalDate closingDate = closingDateIndex != null ? parseExcelDate(row, closingDateIndex) 
            : (allocationDateIndex != null ? parseExcelDate(row, allocationDateIndex) : null);
        String amountStr = amountIndex != null ? getCellValueSafely(row, amountIndex) : null;
        String addressType = addressTypeIndex != null ? getCellValueSafely(row, addressTypeIndex) : null;
        
        // 🔥 FIX: Validate only truly required fields with null handling
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new RuntimeException("Customer Name is required");
        }
        
        if (productName == null || productName.trim().isEmpty()) {
            throw new RuntimeException("Product is required");
        }
        
        if (stage == null || stage.trim().isEmpty()) {
            stage = "NEW_LEAD"; // 🔥 FIX: Default stage instead of throwing error
        }
        
        // 🔥 ENHANCED LOGGING: Log row data for debugging
        log.info("🔥 EXCEL ROW {} → Customer: {}, Bank: {}, Branch: {}, Taluka: {}, District: {}, Product: {}, Stage: {}", 
                 row.getRowNum(), customerName, bankName, branchName, taluka, district, productName, stage);
        
        // Department logic — normalize to UPPERCASE, fallback to userDepartment if blank
        if (department != null && !department.trim().isEmpty()) {
            department = department.trim().toUpperCase();
        } else {
            department = userDepartment != null ? userDepartment.trim().toUpperCase() : "GEN";
        }
        
        // Find or create client
        Client client = findOrCreateClient(customerName, contactName, department, appNo, ownerUserId);
        
        // Find or create bank from Excel data
        Bank bank = findOrCreateBank(bankName, branchName, taluka, district);
        
        // Create or update address (village can be "-")
        CustomerAddress.AddressType addressTypeEnum = parseAddressType(addressType);
        createOrUpdateAddress(client.getId(), village, taluka, district, addressTypeEnum);
        
        // 🔥 FIX: Remove restrictive duplicate check to ensure all Excel rows are imported
        // Optional<Deal> existingDeal = dealRepository.findByNameAndClientId(productName, client.getId());
        
        // Create deal DTO for both new and update scenarios
        DealDto dealDto = new DealDto();
        dealDto.setName(customerName);
        dealDto.setClientId(client.getId());
        dealDto.setDepartment(department);
        String resolvedStage = resolveStageCode(stage, department);
        dealDto.setStageCode(resolvedStage);
        
        // Set bank information if available
        if (bank != null) {
            dealDto.setBankId(bank.getId()); // 🔥 CRITICAL: Save bankId for frontend mapping
            dealDto.setBranchName(bank.getBranchName());
            // relatedBankName not needed - bankId is sufficient for frontend
        } else {
            dealDto.setBranchName(branchName != null && !branchName.equals("-") && !branchName.trim().isEmpty() ? branchName : null);
        }
        
        // 🔥 FIX: Handle allotmentLetter null safely
        if (allotmentLetter != null && !allotmentLetter.equals("-") && !allotmentLetter.trim().isEmpty()) {
            dealDto.setDescription(allotmentLetter);
        } else {
            dealDto.setDescription(null); // 🔥 skip only this field
        }
        
        // 🔥 FIX: Handle amount null safely
        if (amountStr != null && !amountStr.equals("-") && !amountStr.trim().isEmpty()) {
            try {
                // Remove any non-numeric characters except decimal point
                String cleanAmount = amountStr.replaceAll("[^0-9.]", "");
                if (!cleanAmount.isEmpty()) {
                    dealDto.setValueAmount(new BigDecimal(cleanAmount));
                }
            } catch (NumberFormatException e) {
                dealDto.setValueAmount(BigDecimal.ZERO);
            }
        } else {
            dealDto.setValueAmount(BigDecimal.ZERO); // 🔥 safe default
        }
        
        if (closingDate != null) {
            dealDto.setClosingDate(closingDate);
        }
        
        // Generate dealCode using cache to avoid per-row DB call
        Deal deal = crmMapper.toDealEntity(dealDto);
        deal.setClientId(client.getId()); // ensure clientId is always set explicitly
        if ("CLOSE_WIN".equalsIgnoreCase(resolvedStage) || "CLOSE_LOST".equalsIgnoreCase(resolvedStage)) {
            deal.setMovedToApproval(true);
        }
        String dept = department;
        long count = deptCountCache.computeIfAbsent(dept,
            d -> dealRepository.countByDepartment(d));
        count++;
        deptCountCache.put(dept, count);
        deal.setDealCode(dept + count);
        deal = dealService.create(deal);
        
        // 🔥 FIX: CREATE OR FIND PRODUCT
        Product product = productRepository
            .findByNameIgnoreCase(productName)
            .orElseGet(() -> {
                Product p = new Product();
                p.setName(productName);
                p.setActive(true); // 🔥 FIX: Use setActive not setIsActive
                return productRepository.save(p);
            });

        // 🔥 FIX: CREATE DEAL_PRODUCT (LINK)
        DealProduct dealProduct = new DealProduct();
        dealProduct.setDeal(deal); // 🔥 FIX: Use setDeal not setDealId
        dealProduct.setProduct(product); // 🔥 FIX: Use setProduct not setProductId
        dealProduct.setQuantity(BigDecimal.ONE); // 🔥 FIX: Use BigDecimal for quantity
        dealProduct.setUnitPrice(
            dealDto.getValueAmount() != null ? dealDto.getValueAmount() : BigDecimal.ZERO
        );
        dealProduct.setTotal(dealProduct.getUnitPrice()); // 🔥 FIX: Calculate total properly
        
        // 🔥 CRITICAL FIX: Add to deal's collection for proper relationship
        deal.getDealProducts().add(dealProduct);
        
        dealProductRepository.save(dealProduct);
        
        log.info("✅ Product linked: {} → Deal {} (Product ID: {})", productName, deal.getId(), product.getId());
        
        if (deal.getStageHistory() == null) {
            deal.setStageHistory(new ArrayList<>());
        }
        
        // Note: Stage history creation would require DealStageHistory entity and repository
        // For now, the deal will be created with the correct stageCode
        
        log.info("✅ Created new deal: {} for client: {} (Bank: {} | BankId: {})", 
                 productName, customerName, 
                 bank != null ? bank.getName() : "None", 
                 bank != null ? bank.getId() : "None");
    }

    private Client findOrCreateClient(String name, String contactName, String department, String appNo, Long ownerUserId) {
        // 1. Agr No match (most reliable)
        if (appNo != null && !appNo.trim().isEmpty()) {
            Optional<Client> byAppNo = clientRepository.findByCustomerNumber(appNo.trim());
            if (byAppNo.isPresent()) {
                Client c = byAppNo.get();
                if (!Boolean.TRUE.equals(c.getIsActive())) {
                    c.setIsActive(true);
                    clientRepository.save(c);
                }
                log.debug("Found client by Agr No: {}", appNo);
                return c;
            }
        }

        // 2. Name + Contact fallback
        if (name != null && contactName != null && !contactName.trim().isEmpty()) {
            Optional<Client> byNameContact = clientRepository.findByNameAndContactName(name.trim(), contactName.trim());
            if (byNameContact.isPresent()) {
                Client c = byNameContact.get();
                if (!Boolean.TRUE.equals(c.getIsActive())) {
                    c.setIsActive(true);
                    clientRepository.save(c);
                }
                log.debug("Found client by name+contact: {}", name);
                return c;
            }
        }

        // 3. Name-only fallback (handles re-upload after delete)
        if (name != null) {
            Optional<Client> byName = clientRepository.findByName(name.trim());
            if (byName.isPresent()) {
                Client c = byName.get();
                // Only reuse if this client has no deal yet (avoid wrong reuse)
                boolean hasDeal = !dealRepository.findByClientId(c.getId()).isEmpty();
                if (!hasDeal) {
                    if (!Boolean.TRUE.equals(c.getIsActive())) {
                        c.setIsActive(true);
                        clientRepository.save(c);
                    }
                    log.debug("Found dealless client by name: {}", name);
                    return c;
                }
            }
        }

        // 4. Create new client
        Client client = new Client();
        client.setName(name.trim());
        client.setIsActive(true);
        if (appNo != null && !appNo.trim().isEmpty()) {
            client.setCustomerNumber(appNo.trim());
        }
        if (contactName != null && !contactName.trim().isEmpty()) {
            client.setContactName(contactName.trim());
        }
        if (ownerUserId != null) {
            client.setOwnerId(ownerUserId);
        }
        log.info("Creating new client: {} (Agr No: {})", name, appNo);
        return clientService.createClientEntity(client);
    }

    /**
     * 🔥 FIX: Find existing bank or create new one from Excel data using BankService
     */
    private Bank findOrCreateBank(String bankName, String branchName, String taluka, String district) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return null;
        }

        // 🔥 FIX: Handle null values for branch and taluka
        if (branchName != null && branchName.trim().isEmpty()) {
            branchName = null;
        }
        if (taluka != null && taluka.trim().isEmpty()) {
            taluka = null;
        }

        // Find existing bank — use List to avoid NonUniqueResultException
        List<Bank> matches;
        if (branchName != null && taluka != null) {
            matches = bankRepository.findByNameIgnoreCaseAndBranchNameIgnoreCaseAndTalukaIgnoreCase(
                bankName.trim(), branchName.trim(), taluka.trim());
        } else if (branchName != null) {
            matches = bankRepository.findByNameIgnoreCaseAndBranchNameIgnoreCase(
                bankName.trim(), branchName.trim());
        } else {
            matches = bankRepository.findByNameIgnoreCase(bankName.trim())
                .map(java.util.Collections::singletonList)
                .orElse(java.util.Collections.emptyList());
        }

        if (!matches.isEmpty()) {
            log.debug("Found existing bank: {} ({})", bankName, branchName);
            return matches.get(0);
        }

        // 🔥 FIX: Create new bank using BankService (which now handles duplicates correctly)
        Bank newBank = new Bank();
        newBank.setName(bankName.trim());
        newBank.setBranchName(branchName != null ? branchName.trim() : null);
        newBank.setTaluka(taluka != null ? taluka.trim() : null);
        newBank.setDistrict(district != null ? district.trim() : null);
        newBank.setActive(true);

        try {
            Bank createdBank = bankService.create(newBank);
            log.info("Created new bank: {} ({})", bankName, branchName);
            return createdBank;
        } catch (Exception e) {
            log.error("Failed to create bank: {} ({}) - {}", bankName, branchName, e.getMessage());
            return null; // 🔥 FIX: Return null instead of breaking the import
        }
    }

    /**
     * Parse Excel date properly - handles both numeric Excel dates and string dates
     */

    private void createOrUpdateAddress(Long clientId, String village, String taluka, 
                                     String district, CustomerAddress.AddressType addressType) {
        
        // Check if primary address exists
        Optional<CustomerAddress> existing = customerAddressRepository
            .findByClientIdAndAddressType(clientId, addressType);
        
        CustomerAddress address;
        if (existing.isPresent()) {
            address = existing.get();
        } else {
            address = new CustomerAddress();
            address.setClientId(clientId);
            address.setAddressType(addressType);
        }
        
        // 🔥 FIX: Handle address_line null safely (DB constraint)
        if (village == null || village.equals("-") || village.trim().isEmpty()) {
            address.setAddressLine("N/A"); // 🔥 default value for DB constraint
        } else {
            address.setAddressLine(village);
        }
        // 🔥 FIX: Handle city and state null safely
        address.setCity(taluka != null && !taluka.trim().isEmpty() ? taluka : "N/A");
        address.setState(district != null && !district.trim().isEmpty() ? district : "N/A");
        address.setCountry("India"); // Default
        
        customerAddressRepository.save(address);
    }

    private CustomerAddress.AddressType parseAddressType(String addressType) {
        if (addressType == null || addressType.trim().isEmpty()) {
            return CustomerAddress.AddressType.PRIMARY;
        }
        
        try {
            return CustomerAddress.AddressType.valueOf(addressType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CustomerAddress.AddressType.PRIMARY;
        }
    }

    private String getCellValue(Row row, Map<String, Integer> headerMap, String headerName) {
        String normalizedHeaderName = normalizeHeader(headerName);
        Integer index = headerMap.get(normalizedHeaderName);
        
        // If direct match not found, try variations
        if (index == null) {
            index = findHeaderIndex(headerMap, headerName);
        }
        
        if (index == null) return "";
        
        Cell cell = row.getCell(index);
        return getCellValueAsString(cell);
    }
    
    /**
     * Safe cell value reader - never returns null, returns null for empty cells
     */
    private String getCellValueSafely(Row row, int index) {
        Cell cell = row.getCell(index);
        
        if (cell == null) {
            return null; // 🔥 FIX: Return null instead of "-"
        }
        
        return switch (cell.getCellType()) {
            case STRING -> {
                String value = cell.getStringCellValue().trim();
                yield value.isEmpty() ? null : value; // 🔥 FIX: Return null instead of "-"
            }
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    String value = cell.getStringCellValue().trim();
                    yield value.isEmpty() ? null : value; // 🔥 FIX: Return null instead of "-"
                }
            }
            default -> null; // 🔥 FIX: Return null instead of "-"
        };
    }
    
    /**
     * Parse Excel date properly - handles both numeric Excel dates and string dates
     */
    private LocalDate parseExcelDate(Row row, int index) {
        Cell cell = row.getCell(index);
        
        if (cell == null) {
            return null;
        }
        
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else {
                // It's a regular number, not a date
                return null;
            }
        }
        
        if (cell.getCellType() == CellType.STRING) {
            String dateStr = cell.getStringCellValue().trim();
            if (dateStr.isEmpty()) {
                return null;
            }
            try {
                // Try different date formats
                DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("MM-dd-yyyy")
                };
                
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDate.parse(dateStr, formatter);
                    } catch (Exception ignored) {
                        // Try next format
                    }
                }
                
                log.warn("Could not parse date string: {}", dateStr);
                return null;
            } catch (Exception e) {
                log.warn("Error parsing date string: {}", dateStr, e);
                return null;
            }
        }
        
        return null;
    }
    private Integer findHeaderIndex(Map<String, Integer> headerMap, String headerName) {
        String normalizedRequired = normalizeHeader(headerName);
        
        // Direct match
        if (headerMap.containsKey(normalizedRequired)) {
            return headerMap.get(normalizedRequired);
        }
        
        // Check common variations
        return switch (normalizedRequired) {
            case "district" -> {
                if (headerMap.containsKey("district")) yield headerMap.get("district");
                if (headerMap.containsKey("dist")) yield headerMap.get("dist");
                yield null;
            }
            case "customernumber" -> {
                if (headerMap.containsKey("customernumber")) yield headerMap.get("customernumber");
                if (headerMap.containsKey("customerno")) yield headerMap.get("customerno");
                yield null;
            }
            case "contactname" -> {
                if (headerMap.containsKey("contactname")) yield headerMap.get("contactname");
                if (headerMap.containsKey("contactperson")) yield headerMap.get("contactperson");
                yield null;
            }
            case "bankname" -> {
                if (headerMap.containsKey("bankname")) yield headerMap.get("bankname");
                if (headerMap.containsKey("bank")) yield headerMap.get("bank");
                yield null;
            }
            case "branchname" -> {
                if (headerMap.containsKey("branchname")) yield headerMap.get("branchname");
                if (headerMap.containsKey("branch")) yield headerMap.get("branch");
                yield null;
            }
            case "appno" -> {
                if (headerMap.containsKey("appno")) yield headerMap.get("appno");
                if (headerMap.containsKey("agrno")) yield headerMap.get("agrno");
                if (headerMap.containsKey("agr_no")) yield headerMap.get("agr_no");
                if (headerMap.containsKey("applicationno")) yield headerMap.get("applicationno");
                yield null;
            }
            case "allotmentletter" -> {
                if (headerMap.containsKey("allotmentletter")) yield headerMap.get("allotmentletter");
                if (headerMap.containsKey("allotment")) yield headerMap.get("allotment");
                yield null;
            }
            case "closingdate" -> {
                if (headerMap.containsKey("closingdate")) yield headerMap.get("closingdate");
                yield null;
            }
            case "allocationdate" -> {
                if (headerMap.containsKey("allocationdate")) yield headerMap.get("allocationdate");
                if (headerMap.containsKey("allocation")) yield headerMap.get("allocation");
                if (headerMap.containsKey("allocationdate")) yield headerMap.get("allocationdate");
                yield null;
            }
            case "accountstatus" -> {
                if (headerMap.containsKey("accountstatus")) yield headerMap.get("accountstatus");
                if (headerMap.containsKey("account_status")) yield headerMap.get("account_status");
                yield null;
            }
            default -> headerMap.get(normalizedRequired);
        };
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (!value.isEmpty()) return false;
            }
        }
        return true;
    }

    public byte[] generateTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Deals Import");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Allocation Date", "App No", "Customer Name", "Village", "Taluka", "District",
                "Bank Name", "Branch Name", "Contact Name", "Department", "Product",
                "Allotment Letter", "Stage", "Closing Date", "Amount", "Address Type"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create example row
            Row exampleRow = sheet.createRow(1);
            String[] example = {
                "2024-01-15", "APP001", "John Doe", "Village Name", "Taluka Name", "District Name",
                "State Bank", "Main Branch", "John Contact", "PPO", "Home Loan", 
                "Allotment letter details", "LOD", "2024-12-31", "500000", "PRIMARY"
            };
            
            for (int i = 0; i < example.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(example[i]);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
