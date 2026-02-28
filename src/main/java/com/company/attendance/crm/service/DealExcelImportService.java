package com.company.attendance.crm.service;

import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.dto.DealDto;
import com.company.attendance.crm.entity.Deal;
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
    private final ClientService clientService;
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
                headerMap.put(header.toLowerCase(), cell.getColumnIndex());
            }
        }
        
        // Check required headers
        for (String required : REQUIRED_HEADERS) {
            if (!headerMap.containsKey(required.toLowerCase())) {
                throw new RuntimeException("Missing required header: " + required);
            }
        }
        
        return headerMap;
    }

    @Transactional
    private void importSingleRow(Row row, Map<String, Integer> headerMap, 
                                String userDepartment, boolean allowDepartmentOverride) throws Exception {
        
        // Extract values
        String customerName = getCellValue(row, headerMap, "Customer Name");
        String village = getCellValue(row, headerMap, "Village");
        String taluka = getCellValue(row, headerMap, "Taluka");
        String district = getCellValue(row, headerMap, "District");
        String productName = getCellValue(row, headerMap, "Product");
        String department = getCellValue(row, headerMap, "Department");
        String stage = getCellValue(row, headerMap, "Stage");
        
        // Optional fields
        String appNo = getCellValue(row, headerMap, "App No");
        String bankName = getCellValue(row, headerMap, "Bank Name");
        String branchName = getCellValue(row, headerMap, "Branch Name");
        String contactName = getCellValue(row, headerMap, "Contact Name");
        String allotmentLetter = getCellValue(row, headerMap, "Allotment Letter");
        String closingDateStr = getCellValue(row, headerMap, "Closing Date");
        String amountStr = getCellValue(row, headerMap, "Amount");
        String addressType = getCellValue(row, headerMap, "Address Type");
        
        // Department logic
        if (!allowDepartmentOverride || department.isEmpty()) {
            department = userDepartment;
        }
        
        // Find or create client
        Client client = findOrCreateClient(customerName, department);
        
        // Create or update address
        CustomerAddress.AddressType addressTypeEnum = parseAddressType(addressType);
        createOrUpdateAddress(client.getId(), village, taluka, district, addressTypeEnum);
        
        // Create deal
        DealDto dealDto = new DealDto();
        dealDto.setName(productName);
        dealDto.setClientId(client.getId());
        dealDto.setDepartment(department);
        dealDto.setStage(stage);
        dealDto.setRelatedBankName(bankName);
        dealDto.setBranchName(branchName);
        dealDto.setDescription(allotmentLetter);
        
        if (!amountStr.isEmpty()) {
            try {
                dealDto.setValueAmount(new BigDecimal(amountStr));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid amount format: " + amountStr);
            }
        }
        
        if (!closingDateStr.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                dealDto.setClosingDate(LocalDate.parse(closingDateStr, formatter));
            } catch (Exception e) {
                throw new RuntimeException("Invalid closing date format: " + closingDateStr);
            }
        }
        
        // Use existing DealService to create the deal
        Deal deal = crmMapper.toDealEntity(dealDto);
        dealService.create(deal);
        
        log.info("Imported deal: {} for client: {}", productName, customerName);
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
        Integer index = headerMap.get(headerName.toLowerCase());
        if (index == null) return "";
        
        Cell cell = row.getCell(index);
        return getCellValueAsString(cell);
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
