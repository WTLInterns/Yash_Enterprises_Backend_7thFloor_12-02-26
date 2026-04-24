package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.entity.StageMaster;
import com.company.attendance.crm.repository.BankRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.DealProductRepository;
import com.company.attendance.crm.repository.ProductRepository;
import com.company.attendance.crm.repository.StageMasterRepository;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.ExpenseRepository;

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

    // ── Repositories (direct DB access — no geocoding, no proxy issues) ──────
    private final ClientRepository       clientRepository;
    private final BankRepository         bankRepository;
    private final DealRepository         dealRepository;
    private final ProductRepository      productRepository;
    private final DealProductRepository  dealProductRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final StageMasterRepository  stageMasterRepository;
    private final ExpenseRepository      expenseRepository;

    // ── Services (only for deal code generation) ─────────────────────────────
    private final DealService dealService;

    // Per-upload in-memory caches — passed as parameters, NOT class fields (thread-safe)
    // bankCache and productCache are now local to each importDealsFromExcel call

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> importDealsFromExcel(
            org.springframework.web.multipart.MultipartFile file,
            String userDepartment,
            boolean allowDepartmentOverride,
            Long ownerUserId) throws Exception {

        List<String> errors = new ArrayList<>();
        int success = 0, skipped = 0, total = 0;

        // Local caches per upload — thread-safe, no stale data between uploads
        Map<String, Bank>    bankCache    = new HashMap<>();
        Map<String, Product> productCache = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new RuntimeException("Excel has no header row");

            Map<String, Integer> headers = buildHeaderMap(headerRow);
            log.info("Excel headers found: {}", headers.keySet());

            // Per-upload dept count cache (avoids N DB calls for dealCode generation)
            Map<String, Long> deptCountCache = new HashMap<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                total++;
                try {
                    boolean wasSkipped = processRow(row, headers, userDepartment, allowDepartmentOverride,
                               deptCountCache, ownerUserId, bankCache, productCache);
                    if (wasSkipped) {
                        skipped++;
                        log.info("⏭ Row {} skipped (duplicate)", i + 1);
                    } else {
                        success++;
                        log.info("✅ Row {} imported successfully", i + 1);
                    }
                } catch (Exception e) {
                    String msg = "Row " + (i + 1) + ": " + e.getMessage();
                    errors.add(msg);
                    log.error("❌ Row {} failed: {}", i + 1, e.getMessage(), e);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRows", total);
        result.put("success",   success);
        result.put("skipped",   skipped);
        result.put("failed",    total - success - skipped);
        result.put("errors",    errors);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROCESS ONE ROW  — each row is its own transaction
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public boolean processRow(Row row, Map<String, Integer> headers,
                           String userDepartment, boolean allowDepartmentOverride,
                           Map<String, Long> deptCountCache, Long ownerUserId,
                           Map<String, Bank> bankCache, Map<String, Product> productCache) {

        // ── 1. Read all columns ───────────────────────────────────────────────
        String customerName  = str(row, headers, "customername", "customer name", "name");
        String productName   = str(row, headers, "product", "productname", "product name", "loantype", "loan type");
        String stageRaw      = str(row, headers, "stage", "status", "dealstage");
        String bankName      = str(row, headers, "bankname", "bank name", "bank");
        String branchName    = str(row, headers, "branchname", "branch name", "branch");
        String taluka        = str(row, headers, "taluka", "tehsil");
        String district      = str(row, headers, "district", "dist");
        String village       = str(row, headers, "village", "address", "addressline");
        String contactName   = str(row, headers, "contactname", "contact name", "contactperson");
        String appNo         = str(row, headers, "appno", "agrno", "agr no", "app no", "applicationno");
        String deptRaw       = str(row, headers, "department", "dept");
        String amountStr     = str(row, headers, "amount", "loanamount", "loan amount");
        String allotment     = str(row, headers, "allotmentletter", "allotment letter", "allotment");
        String addressType   = str(row, headers, "addresstype", "address type");
        LocalDate closingDate = date(row, headers, "closingdate", "closing date", "allocationdate", "allocation date");

        // ── 2. Validate required fields ───────────────────────────────────────
        if (blank(customerName)) throw new RuntimeException("Customer Name is required");
        if (blank(productName))  throw new RuntimeException("Product is required");
        if (blank(stageRaw))     stageRaw = "NEW_LEAD";

        // ── 3. Resolve department ─────────────────────────────────────────────
        String department;
        if (!blank(deptRaw) && allowDepartmentOverride) {
            department = deptRaw.trim().toUpperCase();
        } else if (!blank(userDepartment)) {
            department = userDepartment.trim().toUpperCase();
        } else if (!blank(deptRaw)) {
            department = deptRaw.trim().toUpperCase();
        } else {
            department = "GEN";
        }

        log.info("Row {} → Customer: {}, Bank: {}, Branch: {}, Dept: {}, Stage: {}, Product: {}",
                 row.getRowNum() + 1, customerName, bankName, branchName, department, stageRaw, productName);

        // ── 4. CREATE / FIND CLIENT ───────────────────────────────────────────
        Client client = findOrCreateClient(customerName, contactName, appNo, ownerUserId, department);

        // Skip ONLY if this exact client already has a deal in this exact department
        boolean dealExists = !dealRepository.findByClientIdAndDepartment(client.getId(), department).isEmpty();
        if (dealExists) {
            log.info("⏭ Skipping row {} — client '{}' (id={}) already has deal in dept '{}'",
                     row.getRowNum() + 1, customerName, client.getId(), department);
            return true; // skipped
        }
        log.info("✅ Row {} — client '{}' (id={}) has NO deal in dept '{}' → creating deal",
                 row.getRowNum() + 1, customerName, client.getId(), department);

        // ── 5. CREATE / FIND BANK ─────────────────────────────────────────────
        Bank bank = findOrCreateBank(bankName, branchName, bankCache);

        // ── 6. CREATE ADDRESS ─────────────────────────────────────────────────
        CustomerAddress.AddressType addrType = parseAddressType(addressType);
        saveAddress(client.getId(), village, taluka, district, addrType);

        // ── 7. CREATE DEAL ────────────────────────────────────────────────────
        BigDecimal amount = parseAmount(amountStr);
        String resolvedStage = resolveStage(stageRaw, department);

        Deal deal = new Deal();
        deal.setName(customerName.trim());
        deal.setClientId(client.getId());
        deal.setDepartment(department);
        deal.setStageCode(resolvedStage);
        deal.setValueAmount(amount);
        if (closingDate != null) deal.setClosingDate(closingDate);
        if (!blank(allotment))   deal.setDescription(allotment.trim());
        if (bank != null) {
            deal.setBankId(bank.getId());
            deal.setBranchName(bank.getBranchName());
            deal.setRelatedBankName(bank.getName());
        } else if (!blank(branchName)) {
            deal.setBranchName(branchName.trim());
        }
        if (ownerUserId != null) deal.setCreatedBy(ownerUserId);
        if ("CLOSE_WIN".equalsIgnoreCase(resolvedStage) || "CLOSE_LOST".equalsIgnoreCase(resolvedStage)) {
            deal.setMovedToApproval(true);
        }

        // Generate dealCode using cache
        long count = deptCountCache.computeIfAbsent(department,
            d -> dealRepository.countByDepartment(d));
        count++;
        deptCountCache.put(department, count);
        deal.setDealCode(department + count);

        deal = dealRepository.save(deal);
        log.info("✅ Deal created: id={}, code={}, client={}", deal.getId(), deal.getDealCode(), customerName);

        // ── Re-link existing expenses by client name + department ──────────────
        relinkExpenses(customerName.trim(), department, client.getId(), deal.getId());

        // ── 8. CREATE / FIND PRODUCT ──────────────────────────────────────────
        Product product = findOrCreateProduct(productName.trim(), ownerUserId, productCache);

        // ── 9. LINK DEAL ↔ PRODUCT ────────────────────────────────────────────
        DealProduct dp = new DealProduct();
        dp.setDeal(deal);
        dp.setProduct(product);
        dp.setQuantity(BigDecimal.ONE);
        dp.setUnitPrice(amount != null ? amount : BigDecimal.ZERO);
        dp.setDiscount(BigDecimal.ZERO);
        dp.setTax(BigDecimal.ZERO);
        dp.computeTotal();
        dealProductRepository.save(dp);

        log.info("✅ Product '{}' linked to deal {}", productName, deal.getId());
        return false; // not skipped, successfully created
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 4 — CLIENT
    // ─────────────────────────────────────────────────────────────────────────

    private Client findOrCreateClient(String name, String contactName,
                                      String appNo, Long ownerUserId, String department) {
        // a) Match by App/Agr No — most precise
        if (!blank(appNo)) {
            Optional<Client> byNo = clientRepository.findByCustomerNumber(appNo.trim());
            if (byNo.isPresent()) {
                Client c = byNo.get();
                if (!Boolean.TRUE.equals(c.getIsActive())) { c.setIsActive(true); clientRepository.save(c); }
                return c;
            }
        }

        // b) Match by name + department — reuse a client ONLY if they already have a deal
        //    in this exact department. Same name in a different department = new client.
        List<Client> byName = clientRepository.findAllByNameNormalized(name.trim());
        Optional<Client> sameNameSameDept = byName.stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .filter(c -> !dealRepository.findByClientIdAndDepartment(c.getId(), department).isEmpty())
            .findFirst();
        if (sameNameSameDept.isPresent()) {
            log.info("♻ Reusing existing client '{}' (id={}) for dept '{}'",
                     name, sameNameSameDept.get().getId(), department);
            return sameNameSameDept.get();
        }

        // c) Create new client
        Client client = new Client();
        client.setName(name.trim());
        client.setIsActive(true);
        if (!blank(appNo))       client.setCustomerNumber(appNo.trim());
        if (!blank(contactName)) client.setContactName(contactName.trim());
        if (ownerUserId != null) {
            client.setOwnerId(ownerUserId);
            client.setCreatedBy(ownerUserId);
        }
        Client saved = clientRepository.save(client);
        log.info("✅ Client created: id={}, name={}", saved.getId(), name);
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 5 — BANK
    // ─────────────────────────────────────────────────────────────────────────

    // ── Bank name canonical aliases ────────────────────────────────────────
    private static final Map<String, String> BANK_NAME_ALIASES = new HashMap<>();
    private static final Map<String, String> BRANCH_NAME_ALIASES = new HashMap<>();
    static {
        // Bank name aliases → canonical name
        BANK_NAME_ALIASES.put("sbi",                        "State Bank Of India");
        BANK_NAME_ALIASES.put("state bank of india",        "State Bank Of India");
        BANK_NAME_ALIASES.put("ubi",                        "Union Bank Of India");
        BANK_NAME_ALIASES.put("union bank of india",        "Union Bank Of India");
        BANK_NAME_ALIASES.put("smbt",                       "Sahkar Maharshi Bhausaheb Thorat Amrutvahini Sahkari Bank LTD.");
        BANK_NAME_ALIASES.put("sahkar maharshi bhausaheb thorat amrutvahini sahkari bank ltd.", "Sahkar Maharshi Bhausaheb Thorat Amrutvahini Sahkari Bank LTD.");
        BANK_NAME_ALIASES.put("indian bank",                "Indian Bank");
        BANK_NAME_ALIASES.put("aavas financiers limited",   "Aavas Financiers Limited");
        BANK_NAME_ALIASES.put("au small finance",           "Au Small Finance");
        BANK_NAME_ALIASES.put("kogta financiers ltd",       "Kogta Financiers Ltd");
        BANK_NAME_ALIASES.put("tyger finance",              "Tyger Finance");
        BANK_NAME_ALIASES.put("vijay nagri",                "Vijay Nagari Patsanstha");
        BANK_NAME_ALIASES.put("vijay nagri patsanstha",     "Vijay Nagari Patsanstha");
        BANK_NAME_ALIASES.put("vijay nagari patsanstha",    "Vijay Nagari Patsanstha");

        // Branch name aliases → canonical branch
        BRANCH_NAME_ALIASES.put("ahmadnagar",       "Ahmednagar");
        BRANCH_NAME_ALIASES.put("ahilyanagar",       "Ahmednagar");
        BRANCH_NAME_ALIASES.put("aahilyanagar",      "Ahmednagar");
        BRANCH_NAME_ALIASES.put("ahmenagar",         "Ahmednagar");
        BRANCH_NAME_ALIASES.put("ahmednagar",        "Ahmednagar");
        BRANCH_NAME_ALIASES.put("sarb",              "SARB Pune");
        BRANCH_NAME_ALIASES.put("sarb pune",         "SARB Pune");
        BRANCH_NAME_ALIASES.put("sarb-pune",         "SARB Pune");
        BRANCH_NAME_ALIASES.put("sarb branch",       "SARB Pune");
        BRANCH_NAME_ALIASES.put("arb",               "ARB Pune");
        BRANCH_NAME_ALIASES.put("arb pune",          "ARB Pune");
        BRANCH_NAME_ALIASES.put("arb-pune",          "ARB Pune");
        BRANCH_NAME_ALIASES.put("arb -pune",         "ARB Pune");
        BRANCH_NAME_ALIASES.put("arb- pune",         "ARB Pune");
        BRANCH_NAME_ALIASES.put("racpc wakde",       "RACPC Wakdevadi");
        BRANCH_NAME_ALIASES.put("racpc wakdevadi",   "RACPC Wakdevadi");
    }

    /** Normalize for lookup key: lowercase, collapse spaces, strip punctuation */
    private String normalize(String val) {
        if (val == null) return "";
        return val.trim().toLowerCase().replaceAll("[-_]", " ").replaceAll("\\s+", " ");
    }

    /** Normalize to a compact key for duplicate matching (no spaces/punctuation) */
    private String normKey(String val) {
        if (val == null) return "";
        return val.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    /** Return canonical bank name (handles SBI/sbi/State Bank Of India → same) */
    private String canonicalBankName(String raw) {
        if (blank(raw)) return raw;
        String key = normalize(raw);
        return BANK_NAME_ALIASES.getOrDefault(key, raw.trim());
    }

    /** Return canonical branch name (handles spelling variants) */
    private String canonicalBranchName(String raw) {
        if (blank(raw)) return raw;
        String key = normalize(raw);
        return BRANCH_NAME_ALIASES.getOrDefault(key, raw.trim());
    }

    private Bank findOrCreateBank(String bankName, String branchName, Map<String, Bank> bankCache) {
        if (blank(bankName)) return null;

        // Canonicalize both name and branch before any lookup
        String canonName   = canonicalBankName(bankName);
        String canonBranch = canonicalBranchName(branchName);

        // Compact key for duplicate matching (ignores case/spaces/punctuation)
        String cacheKey = normKey(canonName) + "|" + normKey(canonBranch);

        if (bankCache.containsKey(cacheKey)) return bankCache.get(cacheKey);

        // Load ALL active banks from DB once per upload into cache
        if (bankCache.isEmpty()) {
            bankRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getActive()))
                .forEach(b -> {
                    String cn = canonicalBankName(b.getName());
                    String cb = canonicalBranchName(b.getBranchName());
                    String k  = normKey(cn) + "|" + normKey(cb);
                    bankCache.putIfAbsent(k, b);
                });
            if (bankCache.containsKey(cacheKey)) return bankCache.get(cacheKey);
        }

        // Not found → create with canonical name/branch and active=true
        Bank bank = new Bank();
        bank.setName(canonName);
        bank.setBranchName(blank(canonBranch) ? null : canonBranch);
        bank.setActive(true);
        Bank saved = bankRepository.save(bank);
        bankCache.put(cacheKey, saved);
        log.info("✅ Bank created: id={}, name={}, branch={}", saved.getId(), canonName, canonBranch);
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 8 — PRODUCT
    // ─────────────────────────────────────────────────────────────────────────

    private Product findOrCreateProduct(String productName, Long ownerUserId, Map<String, Product> productCache) {
        String cacheKey = productName.toLowerCase();
        if (productCache.containsKey(cacheKey)) return productCache.get(cacheKey);

        Optional<Product> existing = productRepository.findByNameIgnoreCase(productName);
        if (existing.isPresent()) {
            productCache.put(cacheKey, existing.get());
            return existing.get();
        }

        Product p = new Product();
        p.setName(productName);
        p.setActive(true);
        if (ownerUserId != null) p.setCreatedBy(ownerUserId);
        Product saved = productRepository.save(p);
        productCache.put(cacheKey, saved);
        log.info("✅ Product created: id={}, name={}", saved.getId(), productName);
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADDRESS
    // ─────────────────────────────────────────────────────────────────────────

    private void relinkExpenses(String clientName, String department, Long newClientId, Long newDealId) {
        try {
            List<com.company.attendance.entity.Expense> orphaned =
                expenseRepository.findByClientNameAndDepartment(clientName, department);
            if (orphaned.isEmpty()) return;
            int relinked = 0;
            for (com.company.attendance.entity.Expense exp : orphaned) {
                boolean needsUpdate = false;
                if (exp.getClientId() == null || !clientRepository.existsById(exp.getClientId())) {
                    exp.setClientId(newClientId);
                    needsUpdate = true;
                }
                if (exp.getDealId() == null || !dealRepository.existsById(exp.getDealId())) {
                    exp.setDealId(newDealId);
                    needsUpdate = true;
                }
                if (needsUpdate) { expenseRepository.save(exp); relinked++; }
            }
            if (relinked > 0)
                log.info("\u2705 Re-linked {} expense(s) for client='{}' dept='{}' -> clientId={} dealId={}",
                    relinked, clientName, department, newClientId, newDealId);
        } catch (Exception e) {
            log.warn("\u26a0\ufe0f Failed to re-link expenses for client='{}': {}", clientName, e.getMessage());
        }
    }

    private void saveAddress(Long clientId, String village, String taluka,
                             String district, CustomerAddress.AddressType type) {
        boolean exists = customerAddressRepository
            .findByClientIdAndAddressType(clientId, type).isPresent();
        if (exists) return;

        CustomerAddress addr = new CustomerAddress();
        addr.setClientId(clientId);
        addr.setAddressType(type);
        addr.setAddressLine(blank(village)  ? "N/A" : village.trim());
        addr.setCity(blank(district)        ? null  : district.trim());   // district = city-level in Maharashtra
        addr.setState(blank(district)       ? "Maharashtra" : "Maharashtra"); // always Maharashtra
        addr.setTaluka(blank(taluka)        ? null  : taluka.trim());
        addr.setDistrict(blank(district)    ? null  : district.trim());
        addr.setCountry("India");
        customerAddressRepository.save(addr);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE RESOLUTION
    // ─────────────────────────────────────────────────────────────────────────

    private String resolveStage(String raw, String department) {
        if (blank(raw)) return "NEW_LEAD";
        String upper = raw.trim().toUpperCase();

        List<StageMaster> stages = stageMasterRepository.findByDepartmentOrderByStageOrder(department);
        for (StageMaster s : stages) {
            if (s.getStageCode().equalsIgnoreCase(upper)) return s.getStageCode();
        }
        String norm = upper.replace(" ", "").replace("_", "");
        for (StageMaster s : stages) {
            String sn = s.getStageName().toUpperCase().replace(" ", "").replace("_", "");
            String sc = s.getStageCode().toUpperCase().replace("_", "");
            if (sn.equals(norm) || sc.equals(norm)) return s.getStageCode();
        }
        // Fallback normalisation
        return switch (upper.replace(" ", "_")) {
            case "NEW_LEAD", "LEAD", "NEW_LEADS"                    -> "NEW_LEAD";
            case "DOC_COLLECT", "DOCUMENT_COLLECTION", "DOC.COLLECT" -> "DOC_COLLECT";
            case "ACCOUNT"                                           -> "ACCOUNT";
            case "DOP", "PDO"                                        -> upper.replace(" ", "_");
            case "CLOSE_WIN", "CLOSED_WON", "CLOSEWIN"              -> "CLOSE_WIN";
            case "CLOSE_LOST", "CLOSED_LOST", "CLOSELOST"           -> "CLOSE_LOST";
            default                                                  -> upper.replace(" ", "_");
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Build a normalised header → column-index map */
    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Cell cell : headerRow) {
            String h = cellStr(cell).trim().toLowerCase().replaceAll("\\s+", "");
            if (!h.isEmpty()) map.put(h, cell.getColumnIndex());
        }
        return map;
    }

    /** Read a cell as String, trying multiple header aliases */
    private String str(Row row, Map<String, Integer> headers, String... aliases) {
        for (String alias : aliases) {
            Integer idx = headers.get(alias.toLowerCase().replaceAll("\\s+", ""));
            if (idx != null) {
                String v = cellStr(row.getCell(idx)).trim();
                if (!v.isEmpty()) return v;
            }
        }
        return null;
    }

    /** Read a cell as LocalDate, trying multiple header aliases */
    private LocalDate date(Row row, Map<String, Integer> headers, String... aliases) {
        for (String alias : aliases) {
            Integer idx = headers.get(alias.toLowerCase().replaceAll("\\s+", ""));
            if (idx == null) continue;
            Cell cell = row.getCell(idx);
            if (cell == null) continue;
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            String s = cellStr(cell).trim();
            if (s.isEmpty()) continue;
            for (String fmt : new String[]{"yyyy-MM-dd","dd/MM/yyyy","dd-MM-yyyy","MM/dd/yyyy"}) {
                try { return LocalDate.parse(s, DateTimeFormatter.ofPattern(fmt)); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private String cellStr(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                            ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                            : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> { try { yield String.valueOf((long) cell.getNumericCellValue()); }
                              catch (Exception e) { yield cell.getStringCellValue(); } }
            default      -> "";
        };
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }

    private BigDecimal parseAmount(String s) {
        if (blank(s)) return BigDecimal.ZERO;
        try { return new BigDecimal(s.replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private CustomerAddress.AddressType parseAddressType(String s) {
        if (blank(s)) return CustomerAddress.AddressType.PRIMARY;
        try { return CustomerAddress.AddressType.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return CustomerAddress.AddressType.PRIMARY; }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            if (!cellStr(row.getCell(i)).trim().isEmpty()) return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEMPLATE GENERATOR
    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generateTemplate() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Deals Import");
            String[] headers = {
                "Allocation Date","App No","Customer Name","Village","Taluka","District",
                "Bank Name","Branch Name","Contact Name","Department","Product",
                "Allotment Letter","Stage","Closing Date","Amount","Address Type"
            };
            Row hr = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) hr.createCell(i).setCellValue(headers[i]);

            Row ex = sheet.createRow(1);
            String[] example = {
                "2024-01-15","APP001","John Doe","Village Name","Taluka Name","District Name",
                "State Bank","Main Branch","John Contact","PPO","Home Loan",
                "Allotment details","NEW_LEAD","2024-12-31","500000","PRIMARY"
            };
            for (int i = 0; i < example.length; i++) ex.createCell(i).setCellValue(example[i]);
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }
}
