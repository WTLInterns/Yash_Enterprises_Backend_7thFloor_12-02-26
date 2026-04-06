package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.BankDto;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.BankContactPerson;
import com.company.attendance.crm.entity.BankDocument;
import com.company.attendance.crm.entity.BankEmailLog;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.mapper.SimpleCrmMapper;
import com.company.attendance.crm.repository.BankContactPersonRepository;
import com.company.attendance.crm.repository.BankDocumentRepository;
import com.company.attendance.crm.repository.BankEmailLogRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.service.AuditService;
import com.company.attendance.crm.service.BankService;
import com.company.attendance.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Banks")
@RestController
@RequestMapping("/api/banks")
public class BankController {
    private final BankService bankService;
    private final DealRepository dealRepository;
    private final SimpleCrmMapper simpleCrmMapper;
    private final AuditService auditService;
    private final BankContactPersonRepository contactPersonRepository;
    private final BankDocumentRepository bankDocumentRepository;
    private final BankEmailLogRepository bankEmailLogRepository;

    public BankController(BankService bankService, DealRepository dealRepository, SimpleCrmMapper simpleCrmMapper,
                          AuditService auditService, BankContactPersonRepository contactPersonRepository,
                          BankDocumentRepository bankDocumentRepository, BankEmailLogRepository bankEmailLogRepository) {
        this.bankService = bankService;
        this.dealRepository = dealRepository;
        this.simpleCrmMapper = simpleCrmMapper;
        this.auditService = auditService;
        this.contactPersonRepository = contactPersonRepository;
        this.bankDocumentRepository = bankDocumentRepository;
        this.bankEmailLogRepository = bankEmailLogRepository;
    }

    @PostMapping
    public ResponseEntity<BankDto> create(@RequestBody BankDto bankDto){
        Bank bank = simpleCrmMapper.toBankEntity(bankDto);
        Bank created = bankService.create(bank);
        BankDto response = simpleCrmMapper.toBankDto(created);
        // Owner is whoever just created the bank
        Long ownerId = created.getUpdatedBy() != null ? created.getUpdatedBy() : created.getCreatedBy();
        response.setOwnerName(auditService.getUserName(ownerId));
        response.setCreatedByName(auditService.getUserName(created.getCreatedBy()));
        response.setUpdatedByName(auditService.getUserName(created.getUpdatedBy()));
        return ResponseEntity.created(URI.create("/api/banks/"+created.getId())).body(response);
    }

    @GetMapping
    public Page<BankDto> list(@RequestParam(value = "active", required = false) Boolean active,
                           @RequestParam(value = "ownerId", required = false) Long ownerId,
                           @RequestParam(value = "q", required = false) String q,
                           Pageable pageable){
        // Default to active=true unless explicitly requested otherwise
        if (active == null && ownerId == null && (q == null || q.isBlank())) {
            Page<Bank> banks = bankService.list(pageable); // This returns only active=true
            List<BankDto> dtos = banks.getContent().stream()
                .map(bank -> {
                    BankDto dto = simpleCrmMapper.toBankDto(bank);
                    Long owner = bank.getUpdatedBy() != null ? bank.getUpdatedBy() : bank.getCreatedBy();
                    dto.setOwnerName(auditService.getUserName(owner));
                    dto.setCreatedByName(auditService.getUserName(bank.getCreatedBy()));
                    dto.setUpdatedByName(auditService.getUserName(bank.getUpdatedBy()));
                    return dto;
                })
                .collect(Collectors.toList());
            return new PageImpl<>(dtos, pageable, banks.getTotalElements());
        }
        Boolean effectiveActive = (active == null ? Boolean.TRUE : active);
        Page<Bank> banks = bankService.search(effectiveActive, ownerId, q, pageable);
        List<BankDto> dtos = banks.getContent().stream()
            .map(bank -> {
                BankDto dto = simpleCrmMapper.toBankDto(bank);
                Long owner = bank.getUpdatedBy() != null ? bank.getUpdatedBy() : bank.getCreatedBy();
                dto.setOwnerName(auditService.getUserName(owner));
                dto.setCreatedByName(auditService.getUserName(bank.getCreatedBy()));
                dto.setUpdatedByName(auditService.getUserName(bank.getUpdatedBy()));
                return dto;
            })
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, banks.getTotalElements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankDto> getById(@PathVariable Long id) {
        Bank bank = bankService.get(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bank not found"));
        BankDto bankDto = simpleCrmMapper.toBankDto(bank);
        Long owner = bank.getUpdatedBy() != null ? bank.getUpdatedBy() : bank.getCreatedBy();
        bankDto.setOwnerName(auditService.getUserName(owner));
        bankDto.setCreatedByName(auditService.getUserName(bank.getCreatedBy()));
        bankDto.setUpdatedByName(auditService.getUserName(bank.getUpdatedBy()));
        return ResponseEntity.ok(bankDto);
    }

    @PutMapping("/{id}")
    public BankDto update(@PathVariable Long id, @RequestBody BankDto bankDto){
        Bank updated = bankService.update(id, bankDto);
        BankDto dto = simpleCrmMapper.toBankDto(updated);
        Long owner = updated.getUpdatedBy() != null ? updated.getUpdatedBy() : updated.getCreatedBy();
        dto.setOwnerName(auditService.getUserName(owner));
        dto.setCreatedByName(auditService.getUserName(updated.getCreatedBy()));
        dto.setUpdatedByName(auditService.getUserName(updated.getUpdatedBy()));
        return dto;
    }

    @PatchMapping("/{id}/status")
    public Bank patchStatus(@PathVariable Long id, @RequestParam("active") boolean active){
        return bankService.patchStatus(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        bankService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // lookup for dropdowns
    @GetMapping("/lookup")
    public List<Map<String, Object>> lookup(@RequestParam(value = "q", required = false) String q, Pageable pageable){
        Page<Bank> page = bankService.search(true, null, q, pageable);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Bank b : page.getContent()){
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("name", b.getName());
            items.add(m);
        }
        return items;
    }

    // usage
    @GetMapping("/{bankId}/deals")
    public List<Map<String, Object>> usage(@PathVariable Long bankId){
        List<Deal> deals = dealRepository.findByClientId(bankId);
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Deal d : deals){
            Map<String, Object> row = new HashMap<>();
            row.put("dealId", d.getId());
            row.put("dealName", d.getName());
            row.put("amount", d.getValueAmount());
            row.put("stage", d.getStageCode());
            resp.add(row);
        }
        return resp;
    }

    // ── Contact Persons ──────────────────────────────────────────────────────

    @GetMapping("/{id}/contacts")
    public ResponseEntity<List<Map<String, Object>>> getContacts(@PathVariable Long id) {
        List<BankContactPerson> contacts = contactPersonRepository.findByBankId(id);
        List<Map<String, Object>> result = contacts.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("fullName", c.getFullName());
            m.put("email", c.getEmail());
            m.put("position", c.getPosition());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/contacts")
    public ResponseEntity<Map<String, Object>> addContact(@PathVariable Long id,
                                                          @RequestBody Map<String, String> body) {
        Bank bank = bankService.get(id).orElseThrow(() -> new ResourceNotFoundException("Bank not found"));
        BankContactPerson cp = new BankContactPerson();
        cp.setBank(bank);
        cp.setFullName(body.get("fullName"));
        cp.setEmail(body.get("email"));
        cp.setPosition(body.get("position"));
        BankContactPerson saved = contactPersonRepository.save(cp);
        Map<String, Object> m = new HashMap<>();
        m.put("id", saved.getId());
        m.put("fullName", saved.getFullName());
        m.put("email", saved.getEmail());
        m.put("position", saved.getPosition());
        return ResponseEntity.status(201).body(m);
    }

    @PutMapping("/{id}/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> updateContact(@PathVariable Long id,
                                                             @PathVariable Long contactId,
                                                             @RequestBody Map<String, String> body) {
        BankContactPerson cp = contactPersonRepository.findById(contactId)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        if (body.containsKey("fullName")) cp.setFullName(body.get("fullName"));
        if (body.containsKey("email")) cp.setEmail(body.get("email"));
        if (body.containsKey("position")) cp.setPosition(body.get("position"));
        BankContactPerson saved = contactPersonRepository.save(cp);
        Map<String, Object> m = new HashMap<>();
        m.put("id", saved.getId());
        m.put("fullName", saved.getFullName());
        m.put("email", saved.getEmail());
        m.put("position", saved.getPosition());
        return ResponseEntity.ok(m);
    }

    @DeleteMapping("/{id}/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id, @PathVariable Long contactId) {
        contactPersonRepository.deleteById(contactId);
        return ResponseEntity.noContent().build();
    }

    // ── Documents ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<Map<String, Object>>> getDocuments(@PathVariable Long id) {
        List<BankDocument> docs = bankDocumentRepository.findByBankIdOrderByCreatedAtDesc(id);
        List<Map<String, Object>> result = docs.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("name", d.getName());
            m.put("fileName", d.getFileName());
            m.put("fileUrl", d.getFileUrl());
            m.put("contentType", d.getContentType());
            m.put("fileSize", d.getFileSize());
            m.put("createdAt", d.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) throws Exception {
        Bank bank = bankService.get(id).orElseThrow(() -> new ResourceNotFoundException("Bank not found"));

        // Store file in uploads/banks/{bankId}/
        String uploadDir = "uploads/banks/" + id + "/";
        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        String uniqueName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(uniqueName);
        Files.write(filePath, file.getBytes());

        BankDocument doc = new BankDocument();
        doc.setBank(bank);
        doc.setName(name);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileUrl("/api/banks/" + id + "/documents/file/" + uniqueName);
        doc.setContentType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setUploadedBy(userId);
        BankDocument saved = bankDocumentRepository.save(doc);

        Map<String, Object> m = new HashMap<>();
        m.put("id", saved.getId());
        m.put("name", saved.getName());
        m.put("fileName", saved.getFileName());
        m.put("fileUrl", saved.getFileUrl());
        m.put("contentType", saved.getContentType());
        m.put("fileSize", saved.getFileSize());
        return ResponseEntity.status(201).body(m);
    }

    @GetMapping("/{id}/documents/file/{fileName}")
    public ResponseEntity<byte[]> serveFile(@PathVariable Long id, @PathVariable String fileName) throws Exception {
        Path filePath = Paths.get("uploads/banks/" + id + "/" + fileName);
        if (!Files.exists(filePath)) return ResponseEntity.notFound().build();
        byte[] bytes = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
            .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
            .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
            .body(bytes);
    }

    @DeleteMapping("/{id}/documents/{docId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @PathVariable Long docId) {
        bankDocumentRepository.deleteById(docId);
        return ResponseEntity.noContent().build();
    }

    // ── Emails ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}/emails")
    public ResponseEntity<List<Map<String, Object>>> getEmails(@PathVariable Long id) {
        List<BankEmailLog> logs = bankEmailLogRepository.findByBankIdOrderBySentAtDesc(id);
        List<Map<String, Object>> result = logs.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("toEmail", e.getToEmail());
            m.put("ccEmail", e.getCcEmail());
            m.put("subject", e.getSubject());
            m.put("body", e.getBody());
            m.put("attachmentName", e.getAttachmentName());
            m.put("sentAt", e.getSentAt());
            m.put("status", e.getStatus());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/emails")
    public ResponseEntity<Map<String, Object>> sendEmail(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile attachment,
            @RequestParam("to") String to,
            @RequestParam(value = "cc", required = false) String cc,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) throws Exception {

        String attachName = null;
        String attachPath = null;
        if (attachment != null && !attachment.isEmpty()) {
            String uploadDir = "uploads/banks/" + id + "/emails/";
            Files.createDirectories(Paths.get(uploadDir));
            String uniqueName = System.currentTimeMillis() + "_" + attachment.getOriginalFilename();
            Files.write(Paths.get(uploadDir + uniqueName), attachment.getBytes());
            attachName = attachment.getOriginalFilename();
            attachPath = uploadDir + uniqueName;
        }

        BankEmailLog log = new BankEmailLog();
        log.setBankId(id);
        log.setToEmail(to);
        log.setCcEmail(cc);
        log.setSubject(subject);
        log.setBody(body);
        log.setAttachmentName(attachName);
        log.setAttachmentPath(attachPath);
        log.setSentBy(userId);
        log.setSentAt(OffsetDateTime.now());
        log.setStatus("SENT");
        BankEmailLog saved = bankEmailLogRepository.save(log);

        Map<String, Object> m = new HashMap<>();
        m.put("id", saved.getId());
        m.put("status", saved.getStatus());
        m.put("sentAt", saved.getSentAt());
        return ResponseEntity.status(201).body(m);
    }

    // permissions helper
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Boolean>> permissions(@PathVariable Long id,
                                                            @RequestHeader(value = "X-User-Role", required = false) String role,
                                                            @RequestHeader(value = "X-User-Id", required = false) Integer userId){
        return bankService.get(id).map(b -> {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean isOwner = userId != null && userId.equals(b.getCreatedBy());
            Map<String, Boolean> map = new HashMap<>();
            map.put("canEdit", isAdmin || isOwner);
            map.put("canDelete", isAdmin);
            map.put("canDisable", isAdmin);
            return ResponseEntity.ok(map);
        }).orElse(ResponseEntity.notFound().build());
    }
}
