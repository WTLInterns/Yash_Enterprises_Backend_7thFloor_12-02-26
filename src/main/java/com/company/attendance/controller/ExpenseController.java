package com.company.attendance.controller;

import com.company.attendance.dto.ExpenseDto;
import com.company.attendance.entity.Expense;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.StageMasterRepository;
import com.company.attendance.mapper.ExpenseMapper;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.service.ExpenseService;
import com.company.attendance.crm.service.AuditService;
import com.company.attendance.util.FileUploadUtil;
import com.company.attendance.util.UploadUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;
    private final UploadUtil uploadUtil;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final StageMasterRepository stageMasterRepository;
    private final ClientRepository clientRepository;
    private final DealRepository dealRepository;
    private final com.company.attendance.repository.ExpenseRepository expenseRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ── Validate based on expenseType ──────────────────────────────────────
    // NOTE: For DEAL type, clientId/dept/stage are derived from deal in ExpenseService.save()
    // so we only validate stage here if dept is already present (not required at this point)
    private void validateByType(ExpenseDto dto) {
        String type = dto.getExpenseType();
        if (type == null || type.isBlank()) return;
        switch (type) {
            case "DEAL" -> {
                // dealId is the only hard requirement — clientId/dept/stage derived in service
                if (dto.getDealId() == null)
                    throw new RuntimeException("DEAL expense requires dealId");
                // If dept already present, validate stage belongs to it
                if (dto.getDepartmentName() != null && !dto.getDepartmentName().isBlank())
                    validateStage(dto.getDepartmentName(), dto.getStageCode());
            }
            case "CLIENT" -> {
                if (dto.getClientId() == null)
                    throw new RuntimeException("CLIENT expense requires clientId");
            }
            case "COMPANY" -> {
                dto.setClientId(null);
                dto.setClientName(null);
                dto.setDepartmentName(null);
                dto.setStageCode(null);
            }
            default -> validateStage(dto.getDepartmentName(), dto.getStageCode());
        }
    }

    // ── Validate that stageCode belongs to department ──────────────────────
    private void validateStage(String department, String stageCode) {
        if (department == null || department.isBlank() || stageCode == null || stageCode.isBlank()) return;
        boolean valid = stageMasterRepository
                .findByDepartmentOrderByStageOrder(department)
                .stream()
                .anyMatch(s -> s.getStageCode().equalsIgnoreCase(stageCode));
        if (!valid) {
            throw new RuntimeException("Stage '" + stageCode + "' is not valid for department '" + department + "'");
        }
    }

    // ── Apply client/department/stage from DTO onto entity ─────────────────
    private void applyClientFields(Expense expense, ExpenseDto dto) {
        if (dto.getClientId() != null)     expense.setClientId(dto.getClientId());
        if (dto.getClientName() != null)   expense.setClientName(dto.getClientName());
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isBlank())
            expense.setDepartmentName(dto.getDepartmentName());
        if (dto.getStageCode() != null)    expense.setStageCode(dto.getStageCode());
        if (dto.getDealId() != null)       expense.setDealId(dto.getDealId());
        if (dto.getExpenseType() != null)  expense.setExpenseType(dto.getExpenseType());
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> listExpenses(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long dealId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartmentHeader
    ) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (month != null && year != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        List<Expense> expenses;
        
        // Role-based filtering
        if (userRoleHeader != null) {
            String role = userRoleHeader;
            String department = userDepartmentHeader;
            
            if (role.equals("ADMIN") || role.equals("MANAGER") || "ACCOUNT".equals(department)) {
                expenses = (employeeId != null || status != null || startDate != null || endDate != null)
                        ? expenseService.findFiltered(employeeId, status, startDate, endDate)
                        : expenseService.findAll();
            } else if (role.equals("TL") && department != null) {
                expenses = expenseService.findByDepartment(department);
            } else if (userIdHeader != null) {
                expenses = expenseService.findByEmployeeId(Long.valueOf(userIdHeader));
            } else {
                expenses = (employeeId != null || status != null || startDate != null || endDate != null)
                        ? expenseService.findFiltered(employeeId, status, startDate, endDate)
                        : expenseService.findAll();
            }
        } else {
            expenses = (employeeId != null || status != null || startDate != null || endDate != null)
                    ? expenseService.findFiltered(employeeId, status, startDate, endDate)
                    : expenseService.findAll();
        }
        
        var dtos = expenses.stream().map(expenseMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    // 🔥 NEW: Get expenses by clientId for CRM integration
    @GetMapping(params = "clientId")
    public ResponseEntity<List<ExpenseDto>> getExpensesByClient(@RequestParam Long clientId) {
        List<Expense> expenses = expenseService.findByClientId(clientId);
        var dtos = expenses.stream().map(expenseMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable Long id) {
        return expenseService.findById(id)
                .map(expenseMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ExpenseDto> createExpense(
            @RequestPart("expense") String expenseJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartmentHeader
    ) throws IOException {
        try {
            ExpenseDto dto = objectMapper.readValue(expenseJson, ExpenseDto.class);

            // Security fix: Force employeeId to current user for non-admin roles
            if (userIdHeader != null && userRoleHeader != null) {
                if (!userRoleHeader.equals("ADMIN") && !userRoleHeader.equals("MANAGER") && !"ACCOUNT".equals(userDepartmentHeader)) {
                    dto.setEmployeeId(Long.valueOf(userIdHeader));
                }
            }

            Expense expense = expenseMapper.toEntity(dto);
            expense.setClientId(dto.getClientId());
            expense.setClientName(null);
            expense = expenseService.save(expense);

            if (file != null && !file.isEmpty()) {
                String receiptUrl = uploadUtil.saveFile(file, "expenses");
                expense.setReceiptUrl(receiptUrl);
                expense = expenseService.save(expense);
            }

            if (dto.getClientId() != null) {
                auditService.logActivity(
                        dto.getClientId(),
                        "EXPENSE_ADDED",
                        "Expense added: ₹" + dto.getAmount() + " for " + dto.getCategory(),
                        dto.getEmployeeId()
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(expenseMapper.toDto(expense));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ExpenseDto.builder().status("ERROR").description(ex.getMessage()).build());
        }
    }

    // Legacy endpoint kept for backward compatibility with older clients
    @PostMapping(path = "/legacy", consumes = "multipart/form-data")
    public ResponseEntity<ExpenseDto> createExpenseLegacy(
            @RequestPart("expense") String expenseJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartmentHeader
    ) throws IOException {
        
        ExpenseDto dto = objectMapper.readValue(expenseJson, ExpenseDto.class);
        
        // Security fix: Force employeeId to current user for non-admin roles
        if (userIdHeader != null && userRoleHeader != null) {
            String role = userRoleHeader;
            if (!role.equals("ADMIN") && !role.equals("MANAGER") && !"ACCOUNT".equals(userDepartmentHeader)) {
                // For TL and EMPLOYEE roles, force employeeId to current user
                dto.setEmployeeId(Long.valueOf(userIdHeader));
            }
        }
        
        // 🔥 DEBUG: Check clientId
        System.out.println("CLIENT ID: " + dto.getClientId());
        
        Expense expense = expenseMapper.toEntity(dto);
        
        // 🔥 IMPORTANT: Set clientId from DTO
        expense.setClientId(dto.getClientId());
        expense.setClientName(dto.getClientName());
        
        expense = expenseService.save(expense);
        
        // 🔥 NEW: Log expense to timeline if clientId exists
        if (dto.getClientId() != null) {
            auditService.logActivity(
                dto.getClientId(),
                "EXPENSE_ADDED",
                "Expense added: ₹" + dto.getAmount() + " for " + dto.getCategory(),
                dto.getEmployeeId()
            );
        }
        
        if(file != null && !file.isEmpty()){
            String receiptUrl = uploadUtil.saveFile(file, "expenses");
            expense.setReceiptUrl(receiptUrl);
            expense = expenseService.save(expense);
        }
        
        return ResponseEntity.ok(expenseMapper.toDto(expense));
    }
    @PutMapping(path = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable Long id,
            @RequestPart("expense") String expenseJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        ExpenseDto dto = objectMapper.readValue(expenseJson, ExpenseDto.class);
        validateByType(dto);
        Expense expense = expenseMapper.toEntity(dto);
        expense.setClientId(dto.getClientId());
        expense.setClientName(null);
        Expense updated = expenseService.update(id, expense);
        if (file != null && !file.isEmpty()) {
            String receiptUrl = uploadUtil.saveFile(file, "expenses");
            updated.setReceiptUrl(receiptUrl);
            updated = expenseService.save(updated);
        }
        return ResponseEntity.ok(expenseMapper.toDto(updated));
    }

    @PostMapping("/{id}/evidence")
    public ResponseEntity<ExpenseDto> uploadEvidence(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        String uploadDir = "C:/uploads/expenses/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        long timestamp = System.currentTimeMillis();
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        Path filePath = uploadPath.resolve(timestamp + "_" + id + extension);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Expense expense = expenseService.getById(id);
        expense.setReceiptUrl("/uploads/expenses/" + filePath.getFileName());
        return ResponseEntity.ok(expenseMapper.toDto(expenseService.save(expense)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ExpenseDto> approveExpense(@PathVariable Long id) {
        Expense expense = expenseService.getById(id);
        expense.setStatus("APPROVED");
        return ResponseEntity.ok(expenseMapper.toDto(expenseService.save(expense)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ExpenseDto> rejectExpense(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        Expense expense = expenseService.getById(id);
        expense.setStatus("REJECTED");
        if (body != null && body.get("reason") != null && !body.get("reason").isBlank()) {
            expense.setRejectionReason(body.get("reason"));
        }
        return ResponseEntity.ok(expenseMapper.toDto(expenseService.save(expense)));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<ExpenseDto> markAsPaid(@PathVariable Long id) {
        Expense expense = expenseService.getById(id);
        expense.setStatus("PAID");
        return ResponseEntity.ok(expenseMapper.toDto(expenseService.save(expense)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk upload — template columns:
     * 0: Employee Name
     * 1: Deal Code  (PPE1, PPO2 — PRIMARY; resolves dealId, clientId, dept, stage from deal at time of upload)
     * 2: Client Name (fallback if no dealCode)
     * 3: Department  (PPE / PPO / ACCOUNT / HLC — fallback)
     * 4: Stage Code  (must match department — fallback)
     * 5: Category
     * 6: Description
     * 7: Amount
     * 8: Expense Date (YYYY-MM-DD)
     * 9: Status
     *
     * IMPORTANT: Uses expenseRepository.save() directly to preserve dept/stage set from deal.
     * Do NOT route through expenseService.save() which re-derives from current deal state.
     */
    @PostMapping("/bulk-upload")
    public ResponseEntity<java.util.Map<String, Object>> bulkUpload(
            @RequestParam("file") MultipartFile file
    ) {
        java.util.List<String> errors = new java.util.ArrayList<>();
        try {
            org.apache.poi.ss.usermodel.Workbook workbook =
                new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream());
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int count = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                boolean allEmpty = true;
                for (int c = 0; c <= 9; c++) {
                    if (!getCellStr(row, c).isEmpty()) { allEmpty = false; break; }
                }
                if (allEmpty) continue;
                try {
                    com.company.attendance.entity.Expense e = new com.company.attendance.entity.Expense();
                    e.setEmployeeName(getCellStr(row, 0));

                    // Col 1: Deal Code — PRIMARY source of truth
                    String dealCodeStr = getCellStr(row, 1).trim();
                    log.info("[BULK ROW {}] employeeName='{}' dealCode='{}'", i + 1, getCellStr(row, 0), dealCodeStr);
                    if (!dealCodeStr.isBlank()) {
                        var dealOpt = dealRepository.findByDealCodeIgnoreCase(dealCodeStr);
                        if (dealOpt.isEmpty()) {
                            log.warn("[BULK ROW {}] Deal code NOT FOUND in DB: '{}'", i + 1, dealCodeStr);
                            errors.add("Row " + (i + 1) + ": Deal code not found: '" + dealCodeStr + "'");
                            continue;
                        }
                        var deal = dealOpt.get();
                        log.info("[BULK ROW {}] Deal found: id={} code='{}' dept='{}' dealStage='{}' clientId={}",
                            i + 1, deal.getId(), deal.getDealCode(), deal.getDepartment(),
                            deal.getStageCode(), deal.getClientId());
                        e.setDealId(deal.getId());
                        e.setClientId(deal.getClientId());
                        // Excel col 3 dept takes priority over deal's DB department
                        String excelDept = getCellStr(row, 3).trim();
                        String resolvedDept = !excelDept.isBlank() ? excelDept : deal.getDepartment();
                        e.setDepartmentName(resolvedDept);
                        log.info("[BULK ROW {}] Dept: excel='{}' deal='{}' resolved='{}'",
                            i + 1, excelDept, deal.getDepartment(), resolvedDept);
                        // Excel col 4 stage takes priority over deal's current stage
                        String excelStage = getCellStr(row, 4).trim();
                        if (!excelStage.isBlank()) {
                            e.setStageCode(excelStage);
                            log.info("[BULK ROW {}] Stage from Excel: '{}'", i + 1, excelStage);
                        } else {
                            e.setStageCode(deal.getStageCode());
                            log.info("[BULK ROW {}] Stage from Deal (fallback): '{}'", i + 1, deal.getStageCode());
                        }
                        var cl=clientRepository.findById(deal.getClientId()).orElse(null); if(cl!=null){e.setClientName(cl.getName()); log.info("[BULK ROW {}] Client: id={} name={}",i+1,cl.getId(),cl.getName());}





                    } else {
                        // Col 2: Client Name fallback
                        String clientName = getCellStr(row, 2).trim();
                        if (!clientName.isBlank()) {
                            var clientOpt = clientRepository.findByName(clientName);
                            if (clientOpt.isEmpty()) {
                                errors.add("Row " + (i + 1) + ": Client not found: '" + clientName + "'");
                                continue;
                            }
                            e.setClientId(clientOpt.get().getId());
                            e.setClientName(clientName);
                        }
                        // Col 3: Department, Col 4: Stage (fallback when no deal code)
                        String dept = getCellStr(row, 3).trim();
                        String stageCode = getCellStr(row, 4).trim();
                        if (!dept.isBlank()) {
                            if (stageCode.isBlank()) {
                                errors.add("Row " + (i + 1) + ": Stage code required when department provided");
                                continue;
                            }
                            boolean valid = stageMasterRepository
                                    .findByDepartmentOrderByStageOrder(dept)
                                    .stream()
                                    .anyMatch(s -> s.getStageCode().equalsIgnoreCase(stageCode));
                            if (!valid) {
                                errors.add("Row " + (i + 1) + ": Stage '" + stageCode + "' invalid for dept '" + dept + "'");
                                continue;
                            }
                            e.setDepartmentName(dept);
                            e.setStageCode(stageCode);
                        }
                        e.setExpenseType("DEAL");
                    }

                    e.setCategory(getCellStr(row, 5));
                    e.setDescription(getCellStr(row, 6));

                    String amtStr = getCellStr(row, 7).replaceAll("\\.0$", "");
                    if (!amtStr.isEmpty()) e.setAmount(Double.parseDouble(amtStr));

                    org.apache.poi.ss.usermodel.Cell dateCell = row.getCell(8);
                    if (dateCell != null) {
                        if (dateCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                                && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(dateCell)) {
                            java.util.Date d = dateCell.getDateCellValue();
                            e.setExpenseDate(d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                        } else {
                            String dateStr = getCellStr(row, 8).replaceAll("\\.0$", "");
                            if (!dateStr.isEmpty()) e.setExpenseDate(LocalDate.parse(dateStr));
                        }
                    }
                    if (e.getExpenseDate() == null) e.setExpenseDate(LocalDate.now());

                    String status = getCellStr(row, 9).trim();
                    e.setStatus(status.isEmpty() ? "PENDING" : status.toUpperCase());

                    // Save directly — bypass expenseService.save() to preserve dept/stage from deal snapshot
                    expenseRepository.save(e);
                    log.info("[BULK ROW {}] SAVED: dealId={} clientId={} dept='{}' stage='{}' amount={}",
                        i + 1, e.getDealId(), e.getClientId(), e.getDepartmentName(), e.getStageCode(), e.getAmount());
                    count++;
                } catch (Exception ex) {
                    log.error("[BULK ROW {}] ERROR: {}", i + 1, ex.getMessage(), ex);
                    errors.add("Row " + (i + 1) + ": " + ex.getMessage());
                }
            }
            workbook.close();
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("count", count);
            if (!errors.isEmpty()) result.put("errors", errors);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(java.util.Map.of("success", false, "message", ex.getMessage()));
        }
    }

    private String getCellStr(org.apache.poi.ss.usermodel.Row row, int idx) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(idx);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    java.util.Date d = cell.getDateCellValue();
                    yield d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
