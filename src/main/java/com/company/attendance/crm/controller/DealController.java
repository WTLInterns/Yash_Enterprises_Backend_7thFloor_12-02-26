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
import com.company.attendance.crm.service.ProductLineService;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
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
  private final ProductLineService productLineService;

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
    NotificationService notificationService,
    ProductLineService productLineService
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
    this.productLineService = productLineService;
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
    List<DealDto> content = page.getContent().stream().map(deal -> {
      DealDto dto = mapper.toDealDto(deal);
      // 🔥 NEW: Calculate and set calculatedValue from products
      try {
        BigDecimal calculatedTotal = productLineService.grandTotal(deal.getId());
        if (calculatedTotal != null && calculatedTotal.compareTo(BigDecimal.ZERO) > 0) {
          dto.setCalculatedValue(calculatedTotal);
        } else {
          dto.setCalculatedValue(deal.getValueAmount());
        }
      } catch (Exception e) {
        dto.setCalculatedValue(deal.getValueAmount());
      }
      return dto;
    }).collect(Collectors.toList());
    return new PageImpl<>(content, pageable, page.getTotalElements());
  }

  @GetMapping(params = "clientId")
  public ResponseEntity<List<DealDto>> listByClientId(@RequestParam Long clientId) {
    List<DealDto> deals = dealRepository.findByClientId(clientId)
      .stream()
      .map(mapper::toDealDto)
      .collect(Collectors.toList());
    return ResponseEntity.ok(deals);
  }

  @GetMapping("/all")
  public ResponseEntity<List<DealDto>> getAllDeals() {
    List<DealDto> dtos = dealService.getAllDeals().stream().map(mapper::toDealDto).collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<DealDto> getById(@PathVariable Long id) {
    Optional<Deal> deal = dealRepository.findById(id);
    return deal.map(d -> ResponseEntity.ok(mapper.toDealDto(d))).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/client/{clientId}")
  public ResponseEntity<List<DealDto>> getByClientId(@PathVariable Long clientId) {
    List<DealDto> deals = dealRepository.findByClientId(clientId).stream().map(mapper::toDealDto).collect(Collectors.toList());
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

  @GetMapping("/{dealId}/stages")
  public ResponseEntity<List<Map<String, Object>>> getStages(@PathVariable Long dealId) {
    Deal deal = dealRepository.findByIdSafe(dealId);
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
  public ResponseEntity<DealDto> changeStage(
    @PathVariable Long dealId,
    @RequestBody Map<String, String> body,
    @RequestHeader(value = "X-User-Id", required = false) Long headerUserId
  ) {
    Deal deal = dealRepository.findByIdSafe(dealId);
    String stageRaw = body.get("newStage");
    if (stageRaw == null || stageRaw.isBlank()) {
      stageRaw = body.get("stage");
    }

    if (stageRaw == null || stageRaw.isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    String prevDepartment = deal.getDepartment();
    String prevStageCode = deal.getStageCode();

    // Set the new stage
    deal.setStageCode(stageRaw);

    // AUTO MOVE TO ACCOUNT if final stage reached OR if stage is "ACCOUNT"
    if ("ACCOUNT".equalsIgnoreCase(stageRaw) || stageService.shouldTransitionToAccount(prevDepartment, stageRaw)) {
      // 🔥 FIXED: Prevent duplicate notifications if already in ACCOUNT
      if ("ACCOUNT".equalsIgnoreCase(prevDepartment)) {
        System.out.println("Deal is already in ACCOUNT department, skipping transfer notifications");
        deal.setStageCode(stageRaw);
      } else {
        deal.setDepartment("ACCOUNT");
        deal.setStageCode(stageService.getFirstAccountStage());
        System.out.println("AUTO TRANSITION: Deal " + dealId + 
          " moved from " + prevDepartment + "/" + prevStageCode + 
          " to ACCOUNT/" + deal.getStageCode());
      }
    } else if (body.get("department") != null) {
      // Manual department change (only if not auto-transitioned)
      // 🔥 FIXED: Prevent duplicate notifications if already in target department
      if (body.get("department").equalsIgnoreCase(prevDepartment)) {
        System.out.println("Deal is already in target department " + body.get("department") + ", skipping transfer notifications");
      } else {
        deal.setDepartment(body.get("department"));
      }
    }

    Deal saved = dealRepository.save(deal);

    // FIX: Try body userId first, then header, then audit service
    Long userId = null;
    if (body.get("userId") != null) {
      try {
        userId = Long.valueOf(body.get("userId"));
      } catch (NumberFormatException e) {
        // ignore
      }
    }
    if (userId == null) {
      userId = headerUserId != null ? headerUserId : auditService.getCurrentUserId();
    }
    
    DealStageHistory h = new DealStageHistory();
    h.setDeal(saved);
    h.setPreviousStage(prevStageCode);
    h.setNewStage(saved.getStageCode());
    h.setChangedBy(userId != null ? String.valueOf(userId) : null);
    h.setChangedAt(OffsetDateTime.now());
    dealStageHistoryRepository.save(h);

    // SEND NOTIFICATIONS when deal is transferred to ACCOUNT
    if ("ACCOUNT".equalsIgnoreCase(saved.getDepartment()) && !"ACCOUNT".equalsIgnoreCase(prevDepartment)) {
      try {
        System.out.println("===== SENDING NOTIFICATIONS FOR DEAL TRANSFER =====");
        System.out.println("Deal ID: " + saved.getId());
        System.out.println("Client ID: " + saved.getClientId());
        System.out.println("Previous Department: " + prevDepartment);
        System.out.println("New Department: ACCOUNT");
        
        // 🔥 FIXED: Calculate deal value from products with proper fallback
        BigDecimal dealValue = saved.getValueAmount();
        try {
            // Try to get calculated grand total from products
            BigDecimal calculatedTotal = productLineService.grandTotal(saved.getId());
            if (calculatedTotal != null && calculatedTotal.compareTo(BigDecimal.ZERO) > 0) {
                dealValue = calculatedTotal;
                System.out.println("Deal Value (calculated from products): " + dealValue);
            } else {
                System.out.println("Deal Value (using fallback - no product totals): " + dealValue);
            }
        } catch (Exception e) {
            System.out.println("Deal Value (fallback to valueAmount due to error): " + dealValue);
            System.out.println("Could not calculate from products: " + e.getMessage());
        }
        
        System.out.println("Customer Name: " + saved.getName());
        
        String title = "Deal Sent to Accounts";
        String message = "Deal for customer '" + saved.getName() +
                      "' (Rs." + (dealValue != null ? dealValue : "0") +
                      ") moved from " + prevDepartment +
                      " to ACCOUNT by " + employeeName(userId);

        Map<String, String> data = Map.of(
            "dealId", String.valueOf(saved.getId()),
            "clientId", String.valueOf(saved.getClientId()),
            "fromDepartment", prevDepartment,
            "toDepartment", "ACCOUNT",
            "dealValue", String.valueOf(dealValue),
            "customerName", saved.getName()
        );

        // Notify ADMIN (role-based)
        System.out.println("Sending ADMIN notification...");
        notificationService.sendRoleBasedNotification(
            "ADMIN",
            title,
            message,
            "DEAL_TRANSFER",
            saved.getId()
        );
        System.out.println("✅ ADMIN notification sent");

        // Notify MANAGER (role-based)
        System.out.println("Sending MANAGER notification...");
        notificationService.sendRoleBasedNotification(
            "MANAGER",
            title,
            message,
            "DEAL_TRANSFER",
            saved.getId()
        );
        System.out.println("✅ MANAGER notification sent");

        // Notify ACCOUNT department (department-based)
        System.out.println("Sending ACCOUNT department notification...");
        notificationService.sendDepartmentNotification(
            "ACCOUNT",
            title,
            message,
            "DEAL_TRANSFER",
            saved.getId()
        );
        System.out.println("✅ ACCOUNT department notification sent");

        System.out.println("===== ALL NOTIFICATIONS SENT SUCCESSFULLY =====");
        System.out.println("NOTIFICATIONS SENT: Deal " + saved.getId() + 
          " transfer to ACCOUNT notified to ADMIN, MANAGER, ACCOUNT department");
      } catch (Exception e) {
        System.err.println("===== NOTIFICATION SENDING FAILED =====");
        System.err.println("FAILED TO SEND NOTIFICATIONS: " + e.getMessage());
        e.printStackTrace();
        // Continue even if notifications fail
      }
    }

    return ResponseEntity.ok(mapper.toDealDto(saved));
  }

  @GetMapping("/{dealId}/timeline")
  public ResponseEntity<List<Map<String, Object>>> timeline(@PathVariable Long dealId) {
    Deal deal = dealRepository.findByIdSafe(dealId);

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