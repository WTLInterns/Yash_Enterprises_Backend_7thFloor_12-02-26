package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.DealDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealStageHistory;
import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.repository.ActivityRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.DealStageHistoryRepository;
import com.company.attendance.crm.repository.NoteRepository;
import com.company.attendance.crm.service.DealService;
import com.company.attendance.crm.service.AuditService;
import com.company.attendance.crm.service.StageService;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deals")
public class DealController {
  private final DealService dealService;
  private final DealRepository dealRepository;
  private final DealStageHistoryRepository dealStageHistoryRepository;
  private final NoteRepository noteRepository;
  private final ActivityRepository activityRepository;
  private final EmployeeRepository employeeRepository;
  private final AuditService auditService;
  private final CrmMapper mapper;
  private final StageService stageService;
  private final NotificationService notificationService;

  public DealController(
    DealService dealService,
    DealRepository dealRepository,
    DealStageHistoryRepository dealStageHistoryRepository,
    NoteRepository noteRepository,
    ActivityRepository activityRepository,
    EmployeeRepository employeeRepository,
    AuditService auditService,
    CrmMapper mapper,
    StageService stageService,
    NotificationService notificationService
  ) {
    this.dealService = dealService;
    this.dealRepository = dealRepository;
    this.dealStageHistoryRepository = dealStageHistoryRepository;
    this.noteRepository = noteRepository;
    this.activityRepository = activityRepository;
    this.employeeRepository = employeeRepository;
    this.auditService = auditService;
    this.mapper = mapper;
    this.stageService = stageService;
    this.notificationService = notificationService;
  }

  private String employeeName(Long employeeId) {
    if (employeeId == null) return "System";
    Optional<Employee> emp = employeeRepository.findById(employeeId);
    if (emp.isEmpty()) return "System";
    String first = emp.get().getFirstName() != null ? emp.get().getFirstName().trim() : "";
    String last = emp.get().getLastName() != null ? emp.get().getLastName().trim() : "";
    String full = (first + " " + last).trim();
    return full.isEmpty() ? "System" : full;
  }

  private String employeeNameFromChangedBy(String changedBy) {
    if (changedBy == null || changedBy.isBlank()) return "System";
    try {
      return employeeName(Long.valueOf(changedBy));
    } catch (NumberFormatException ex) {
      return "System";
    }
  }

  @GetMapping
  public Page<DealDto> list(Pageable pageable) {
    Page<Deal> page = dealService.list(pageable);
    List<DealDto> content = page.getContent().stream()
      .map(deal -> {
        DealDto dto = mapper.toDealDto(deal);
        dto.setCalculatedValue(deal.getValueAmount());
        if (dto.getClientId() == null) dto.setClientId(deal.getClientId());
        if (dto.getBankId() == null) dto.setBankId(deal.getBankId());
        if (dto.getBranchName() == null) dto.setBranchName(deal.getBranchName());
        return dto;
      })
      .collect(Collectors.toList());
    return new PageImpl<>(content, pageable, page.getTotalElements());
  }

  @GetMapping(params = "clientId")
  public ResponseEntity<List<DealDto>> listByClientId(@RequestParam Long clientId) {
    List<DealDto> deals = dealRepository.findByClientIdWithRelations(clientId)
      .stream()
      .map(d -> {
        DealDto dto = mapper.toDealDto(d);
        if (dto.getClientId() == null) dto.setClientId(d.getClientId());
        if (dto.getBankId() == null) dto.setBankId(d.getBankId());
        return dto;
      })
      .collect(Collectors.toList());
    return ResponseEntity.ok(deals);
  }

  @GetMapping("/all")
  public ResponseEntity<List<DealDto>> getAllDeals() {
    List<DealDto> dtos = dealService.getAllDeals().stream().map(d -> {
      DealDto dto = mapper.toDealDto(d);
      dto.setCalculatedValue(d.getValueAmount());
      if (dto.getClientId() == null) dto.setClientId(d.getClientId());
      if (dto.getBankId() == null) dto.setBankId(d.getBankId());
      if (dto.getBranchName() == null) dto.setBranchName(d.getBranchName());
      return dto;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/filtered")
  public ResponseEntity<List<DealDto>> getFilteredDeals(
      @RequestHeader(value = "X-User-Role", required = false) String role,
      @RequestHeader(value = "X-User-Department", required = false) String department
  ) {
    List<Deal> deals;
    if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role) || "HR".equalsIgnoreCase(role)) {
      // ADMIN / MANAGER / HR → full access, all deals
      deals = dealRepository.findAllWithClient();
    } else if (department != null && !department.isBlank()) {
      // Department-based users → only their department's deals
      deals = dealRepository.findByDepartment(department.trim().toUpperCase());
    } else {
      deals = List.of();
    }
    List<DealDto> dtos = deals.stream().map(d -> {
      DealDto dto = mapper.toDealDto(d);
      dto.setCalculatedValue(d.getValueAmount());
      if (dto.getClientId() == null && d.getClientId() != null) dto.setClientId(d.getClientId());
      if (dto.getBankId() == null && d.getBankId() != null) dto.setBankId(d.getBankId());
      if (dto.getBranchName() == null && d.getBranchName() != null) dto.setBranchName(d.getBranchName());
      return dto;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<DealDto> getById(@PathVariable Long id) {
    Optional<Deal> deal = dealRepository.findByIdWithRelations(id);
    return deal.map(d -> ResponseEntity.ok(mapper.toDealDto(d))).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/client/{clientId}")
  public ResponseEntity<List<DealDto>> getByClientId(@PathVariable Long clientId) {
    List<DealDto> deals = dealRepository.findByClientIdWithRelations(clientId).stream().map(mapper::toDealDto).collect(Collectors.toList());
    return ResponseEntity.ok(deals);
  }

  @PostMapping
  public ResponseEntity<DealDto> create(@RequestBody DealDto dealDto) {
    Deal deal = mapper.toDealEntity(dealDto);
    Deal created = dealService.create(deal);
    DealDto body = mapper.toDealDto(created);
    return ResponseEntity.created(URI.create("/api/deals/" + created.getId())).body(body);
  }

  @PutMapping("/{id}")
  public ResponseEntity<DealDto> update(@PathVariable Long id, @RequestBody DealDto dealDto) {
    Optional<Deal> existing = dealRepository.findById(id);
    if (existing.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Deal deal = existing.get();

    // Merge only fields provided by DTO (prevent nulling other DB values)
    deal.setClientId(dealDto.getClientId());
    deal.setBankId(dealDto.getBankId()); // IMPORTANT: persist bankId so UI can prefill
    deal.setName(dealDto.getName());
    deal.setValueAmount(dealDto.getValueAmount());
    deal.setClosingDate(dealDto.getClosingDate());
    deal.setBranchName(dealDto.getBranchName());
    deal.setRelatedBankName(dealDto.getRelatedBankName());
    deal.setDescription(dealDto.getDescription());
    deal.setRequiredAmount(dealDto.getRequiredAmount());
    deal.setOutstandingAmount(dealDto.getOutstandingAmount());
    if (dealDto.getStage() != null && !dealDto.getStage().trim().isEmpty()) {
      deal.setStageCode(dealDto.getStage());
    }
    if (dealDto.getDepartment() != null && !dealDto.getDepartment().trim().isEmpty()) {
      deal.setDepartment(dealDto.getDepartment());
    }
    deal.setActive(dealDto.getActive());

    Deal updated = dealService.update(deal);
    return ResponseEntity.ok(mapper.toDealDto(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    dealService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/bulk")
  public ResponseEntity<Map<String, Object>> bulkDelete(@RequestBody Map<String, List<Long>> body) {
    List<Long> ids = body.get("ids");
    if (ids == null || ids.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "No IDs provided"));
    }
    dealService.deleteAll(ids);
    Map<String, Object> result = new HashMap<>();
    result.put("deleted", ids.size());
    result.put("message", ids.size() + " deal(s) deleted successfully");
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{dealId}/stages")
  @Transactional(readOnly = true)
  public ResponseEntity<List<Map<String, Object>>> getStages(@PathVariable Long dealId) {
    Deal deal = dealRepository.findByIdWithRelations(dealId).orElseThrow(() -> new com.company.attendance.exception.ResourceNotFoundException("Deal not found: " + dealId));
    List<DealStageHistory> history = dealStageHistoryRepository.findByDealOrderByChangedAtDesc(deal, Pageable.unpaged()).getContent();
    List<Map<String, Object>> body = history.stream().map(h -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", h.getId() != null ? h.getId().toString() : null);
      m.put("previousStage", h.getPreviousStage());
      m.put("newStage", h.getNewStage());
      m.put("changedAt", h.getChangedAt() != null ? h.getChangedAt().toString() : null);
      m.put("changedBy", h.getChangedBy());
      m.put("changedByName", employeeNameFromChangedBy(h.getChangedBy()));
      return m;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(body);
  }

  @PostMapping("/{dealId}/stages")
  @Transactional
  public ResponseEntity<DealDto> changeStage(
    @PathVariable Long dealId,
    @RequestBody Map<String, String> body,
    @RequestHeader(value = "X-User-Id", required = false) Long headerUserId
  ) {
    Deal deal = dealRepository.findByIdWithRelations(dealId).orElseThrow(() -> new com.company.attendance.exception.ResourceNotFoundException("Deal not found: " + dealId));
    String stageRaw = body.get("newStage");
    if (stageRaw == null || stageRaw.isBlank()) {
      stageRaw = body.get("stage");
    }

    if (stageRaw == null || stageRaw.isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    String prevDepartment = deal.getDepartment();
    String prevStageCode = deal.getStageCode();

    // ✅ FIXED: Directly set new stage — NO auto ACCOUNT transfer
    // Account transfer is now handled via LeadClosureApprovalService (approval flow)
    deal.setStageCode(stageRaw);

    // Only update department if explicitly provided in request body
    if (body.get("department") != null && !body.get("department").isBlank()) {
      deal.setDepartment(body.get("department"));
    }

    Deal saved = dealService.update(deal);

    // Resolve userId from body → header → audit service
    Long userId = null;
    if (body.get("userId") != null) {
      try { userId = Long.valueOf(body.get("userId")); } catch (NumberFormatException e) { /* ignore */ }
    }
    if (userId == null) {
      userId = headerUserId != null ? headerUserId : auditService.getCurrentUserId();
    }
    
    // Save stage history
    DealStageHistory h = new DealStageHistory();
    h.setDeal(saved);
    h.setPreviousStage(prevStageCode);
    h.setNewStage(saved.getStageCode());
    h.setChangedBy(userId != null ? String.valueOf(userId) : null);
    h.setChangedAt(OffsetDateTime.now());
    dealStageHistoryRepository.save(h);

    return ResponseEntity.ok(mapper.toDealDto(saved));
  }

  @GetMapping("/{dealId}/timeline")
  @Transactional(readOnly = true)
  public ResponseEntity<List<Map<String, Object>>> timeline(@PathVariable Long dealId) {
    Deal deal = dealRepository.findByIdWithRelations(dealId).orElseThrow(() -> new com.company.attendance.exception.ResourceNotFoundException("Deal not found: " + dealId));

    List<Map<String, Object>> stageItems = dealStageHistoryRepository
      .findByDealOrderByChangedAtDesc(deal, Pageable.unpaged())
      .getContent()
      .stream()
      .map(h -> {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "STAGE");
        m.put("time", h.getChangedAt() != null ? h.getChangedAt().toString() : null);
        m.put("actor", employeeNameFromChangedBy(h.getChangedBy()));
        m.put("message", h.getNewStage() != null ? ("Stage changed to " + h.getNewStage()) : "Stage changed");
        return m;
      })
      .collect(Collectors.toList());

    List<Map<String, Object>> noteItems = noteRepository
      .findByDealOrderByCreatedAtDesc(deal, Pageable.ofSize(50))
      .getContent()
      .stream()
      .map(n -> {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "NOTE");
        m.put("time", n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
        m.put("actor", employeeName(n.getCreatedBy()));
        m.put("message", n.getTitle() != null ? n.getTitle() : "Note");
        return m;
      })
      .collect(Collectors.toList());

    List<Map<String, Object>> activityItems = activityRepository
      .findByDealOrderByCreatedAtDesc(deal)
      .stream()
      .map(a -> {
        Map<String, Object> m = new HashMap<>();
        m.put("type", a.getType() != null ? a.getType().name() : "ACTIVITY");
        m.put("time", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
        m.put("actor", employeeName(a.getCreatedBy()));
        m.put("message", a.getName() != null ? a.getName() : "Activity");
        return m;
      })
      .collect(Collectors.toList());

    List<Map<String, Object>> all = new java.util.ArrayList<>();
    all.addAll(stageItems);
    all.addAll(noteItems);
    all.addAll(activityItems);
    all.sort((a, b) -> {
      String ta = (String) a.get("time");
      String tb = (String) b.get("time");
      if (ta == null && tb == null) return 0;
      if (ta == null) return 1;
      if (tb == null) return -1;
      return tb.compareTo(ta);
    });

    return ResponseEntity.ok(all);
  }
}