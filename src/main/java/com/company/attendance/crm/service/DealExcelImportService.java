package com.company.attendance.crm.service;

import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.dto.DealDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.repository.BankRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.entity.Employee;
import com.company.attendance.service.ClientService;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.EmployeeRepository;
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
    private final ClientRepository clientRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final EmployeeRepository employeeRepository;
    private final CrmMapper crmMapper;

    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "Customer Name", "Village", "Taluka", "District", 
        "Product", "Department", "Stage"
    );

    @Transactional
    public Map<String, Object> importDealsFromExcel(
            org.springframework.web.multipart.MultipartFile file,
            String userDepartment,
            boolean allowDepartmentOverride) throws Exception {
        
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
            
            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                
                totalRows++;
                
                try {
                    importSingleRow(row, headerMap, userDepartment, allowDepartmentOverride);
                    successCount++;
                } catch (Exception e) {
                    String error = String.format("Row %d: %s", i + 1, e.getMessage());
                    errors.add(error);
                    log.warn("Failed to import row {}: {}", i + 1, e.getMessage());
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
            default -> headerMap.containsKey(normalizedRequired);
        };
    }

    @Transactional
    private void importSingleRow(Row row, Map<String, Integer> headerMap, 
                                String userDepartment, boolean allowDepartmentOverride) throws Exception {
        
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
        Integer amountIndex = findHeaderIndex(headerMap, "Amount");
        Integer addressTypeIndex = findHeaderIndex(headerMap, "Address Type");
        
        // Extract values safely (never null, returns "-" for empty)
        String customerName = customerNameIndex != null ? getCellValueSafely(row, customerNameIndex) : "-";
        String village = villageIndex != null ? getCellValueSafely(row, villageIndex) : "-";
        String taluka = talukaIndex != null ? getCellValueSafely(row, talukaIndex) : "-";
        String district = districtIndex != null ? getCellValueSafely(row, districtIndex) : "-";
        String productName = productNameIndex != null ? getCellValueSafely(row, productNameIndex) : "-";
        String department = departmentIndex != null ? getCellValueSafely(row, departmentIndex) : "-";
        String stage = stageIndex != null ? getCellValueSafely(row, stageIndex) : "-";
        
        // Optional fields
        String appNo = appNoIndex != null ? getCellValueSafely(row, appNoIndex) : "-";
        String bankName = bankNameIndex != null ? getCellValueSafely(row, bankNameIndex) : "-";
        String branchName = branchNameIndex != null ? getCellValueSafely(row, branchNameIndex) : "-";
        String contactName = contactNameIndex != null ? getCellValueSafely(row, contactNameIndex) : "-";
        String allotmentLetter = allotmentLetterIndex != null ? getCellValueSafely(row, allotmentLetterIndex) : "-";
        LocalDate closingDate = closingDateIndex != null ? parseExcelDate(row, closingDateIndex) : null;
        String amountStr = amountIndex != null ? getCellValueSafely(row, amountIndex) : "-";
        String addressType = addressTypeIndex != null ? getCellValueSafely(row, addressTypeIndex) : "-";
        
        // Validate only truly required fields
        if (customerName.equals("-")) {
            throw new RuntimeException("Customer Name is required");
        }
        
        if (productName.equals("-")) {
            throw new RuntimeException("Product is required");
        }
        
        if (stage.equals("-")) {
            throw new RuntimeException("Stage is required");
        }
        
        // Log row data for debugging
        log.debug("Importing row: Customer={}, Village={}, District={}, Product={}, Stage={}", 
                 customerName, village, district, productName, stage);
        
        // Department logic
        if (!allowDepartmentOverride || department.equals("-")) {
            department = userDepartment;
        }
        
        // Find or create client
        Client client = findOrCreateClient(customerName, department);
        
        // Find or create bank from Excel data
        Bank bank = findOrCreateBank(bankName, branchName);
        
        // Create or update address (village can be "-")
        CustomerAddress.AddressType addressTypeEnum = parseAddressType(addressType);
        createOrUpdateAddress(client.getId(), village, taluka, district, addressTypeEnum);
        
        // Check for existing deal to prevent duplicates
        Optional<Deal> existingDeal = dealRepository.findByNameAndClientId(productName, client.getId());
        
        // Create deal DTO for both new and update scenarios
        DealDto dealDto = new DealDto();
        dealDto.setName(productName);
        dealDto.setClientId(client.getId());
        dealDto.setDepartment(department);
        dealDto.setStage(stage);
        
        // Set bank information if available
        if (bank != null) {
            dealDto.setRelatedBankName(bank.getName());
            dealDto.setBranchName(bank.getBranchName());
        } else {
            dealDto.setRelatedBankName(!bankName.equals("-") ? bankName : null);
            dealDto.setBranchName(!branchName.equals("-") ? branchName : null);
        }
        
        dealDto.setDescription(allotmentLetter.equals("-") ? null : allotmentLetter);
        
        // Handle amount safely
        if (!amountStr.equals("-") && !amountStr.isEmpty()) {
            try {
                // Remove any non-numeric characters except decimal point
                String cleanAmount = amountStr.replaceAll("[^0-9.]", "");
                if (!cleanAmount.isEmpty()) {
                    dealDto.setValueAmount(new BigDecimal(cleanAmount));
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid amount format '{}' for customer {}, using 0", amountStr, customerName);
                dealDto.setValueAmount(BigDecimal.ZERO);
            }
        }
        
        // Handle closing date (already parsed safely)
        if (closingDate != null) {
            dealDto.setClosingDate(closingDate);
        }
        
        if (existingDeal.isPresent()) {
            // Update existing deal with missing information
            Deal existing = existingDeal.get();
            boolean updated = false;
            
            if (existing.getBranchName() == null && dealDto.getBranchName() != null) {
                existing.setBranchName(dealDto.getBranchName());
                updated = true;
            }
            
            if (existing.getRelatedBankName() == null && dealDto.getRelatedBankName() != null) {
                existing.setRelatedBankName(dealDto.getRelatedBankName());
                updated = true;
            }
            
            if (existing.getValueAmount() == null && dealDto.getValueAmount() != null) {
                existing.setValueAmount(dealDto.getValueAmount());
                updated = true;
            }
            
            if (existing.getClosingDate() == null && dealDto.getClosingDate() != null) {
                existing.setClosingDate(dealDto.getClosingDate());
                updated = true;
            }
            
            if (existing.getDescription() == null && dealDto.getDescription() != null) {
                existing.setDescription(dealDto.getDescription());
                updated = true;
            }
            
            if (updated) {
                dealRepository.save(existing);
                log.info("Updated existing deal {} for client: {}", existing.getId(), customerName);
            } else {
                log.debug("No updates needed for existing deal {} for client: {}", existing.getId(), customerName);
            }
        } else {
            // Create new deal
            Deal deal = crmMapper.toDealEntity(dealDto);
            dealService.create(deal);
            
            log.info("Created new deal: {} for client: {}", productName, customerName);
        }
    }

    private Client findOrCreateClient(String name, String department) {
        // Note: Client entity doesn't have department field
        // Check if client exists by name only (clients are global)
        Optional<Client> existing = clientRepository.findByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new client
        Client client = new Client();
        client.setName(name);
        client.setIsActive(true);
        
        return clientService.createClientEntity(client);
    }

    /**
     * Find existing bank or create new one from Excel data
     */
    private Bank findOrCreateBank(String bankName, String branchName) {
        if (bankName == null || bankName.equals("-")) {
            return null;
        }

        // Check if bank already exists (case-insensitive)
        Optional<Bank> existing = bankRepository.findByNameIgnoreCase(bankName.trim());
        if (existing.isPresent()) {
            log.debug("Found existing bank: {}", bankName);
            return existing.get();
        }

        // Create new bank
        Bank bank = new Bank();
        bank.setName(bankName.trim());
        bank.setBranchName(branchName != null && !branchName.equals("-") ? branchName.trim() : null);
        bank.setActive(true);
        
        log.info("Creating new bank: {} - {}", bankName, branchName);
        return bankService.create(bank);
    }

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
        
        address.setAddressLine(village);
        address.setCity(taluka);
        address.setState(district);
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
     * Safe cell value reader - never returns null, returns "-" for empty cells
     */
    private String getCellValueSafely(Row row, int index) {
        Cell cell = row.getCell(index);
        
        if (cell == null) {
            return "-";
        }
        
        return switch (cell.getCellType()) {
            case STRING -> {
                String value = cell.getStringCellValue().trim();
                yield value.isEmpty() ? "-" : value;
            }
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue().trim();
                }
            }
            default -> "-";
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
