package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.DealDetailDTO;
import com.company.attendance.crm.dto.StageChangeRequest;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealStageHistory;
import com.company.attendance.crm.entity.EmailRecord;
import com.company.attendance.crm.enums.DealStage;
import com.company.attendance.crm.repository.*;
import com.company.attendance.crm.service.DealService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals")
public class DealController {
    private final DealService dealService;
    private final DealRepository dealRepository;
    private final ProductLineRepository productLineRepository;
    private final FileMetaRepository fileMetaRepository;
    private final NoteRepository noteRepository;
    private final ActivityRepository activityRepository;
    private final EmailRecordRepository emailRecordRepository;

    public DealController(DealService dealService,
                          DealRepository dealRepository,
                          ProductLineRepository productLineRepository,
                          FileMetaRepository fileMetaRepository,
                          NoteRepository noteRepository,
                          ActivityRepository activityRepository,
                          EmailRecordRepository emailRecordRepository) {
        this.dealService = dealService;
        this.dealRepository = dealRepository;
        this.productLineRepository = productLineRepository;
        this.fileMetaRepository = fileMetaRepository;
        this.noteRepository = noteRepository;
        this.activityRepository = activityRepository;
        this.emailRecordRepository = emailRecordRepository;
    }

    @GetMapping
    public Page<Deal> list(@RequestParam(value = "active", required = false) Boolean active,
                           @RequestParam(value = "ownerId", required = false) UUID ownerId,
                           @RequestParam(value = "q", required = false) String q,
                           Pageable pageable) {
        // Default to active=true unless explicitly requested otherwise
        if (active == null && ownerId == null && (q == null || q.isBlank())) {
            return dealService.list(pageable); // This returns only active=true
        }
        Boolean effectiveActive = (active == null ? Boolean.TRUE : active);
        return dealService.search(effectiveActive, ownerId, q, pageable);
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<Deal>> getAllDeals() {
        return ResponseEntity.ok(dealService.getAllDeals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealDetailDTO> get(@PathVariable UUID id) {
        Deal deal = dealRepository.findByIdSafe(id).orElse(null);
        if (deal == null) return ResponseEntity.notFound().build();
        DealDetailDTO dto = new DealDetailDTO();
        dto.id = deal.getId();
        dto.name = deal.getName();
        dto.valueAmount = deal.getValueAmount();
        dto.closingDate = deal.getClosingDate();
        dto.stage = deal.getStage() != null ? deal.getStage().name() : null;
        dto.notesCount = (int) noteRepository.count(); // simplified counts
        dto.activitiesCount = (int) activityRepository.count();
        // products and files lists intentionally omitted in minimal version
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<DealDetailDTO> getByClientId(@PathVariable UUID clientId) {
        Deal deal = dealRepository.findFirstByClientIdOrderByCreatedAtDesc(clientId).orElse(null);
        if (deal == null) return ResponseEntity.notFound().build();
        DealDetailDTO dto = new DealDetailDTO();
        dto.id = deal.getId();
        dto.name = deal.getName();
        dto.valueAmount = deal.getValueAmount();
        dto.closingDate = deal.getClosingDate();
        dto.stage = deal.getStage() != null ? deal.getStage().name() : null;
        dto.notesCount = (int) noteRepository.count();
        dto.activitiesCount = (int) activityRepository.count();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Deal> create(@RequestBody Deal deal, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        Deal created = dealService.create(deal, userId);
        return ResponseEntity.created(URI.create("/api/deals/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deal> update(@PathVariable UUID id, @RequestBody Deal deal,
                                       @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        Deal updated = dealService.update(id, deal, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        dealService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stages")
    public Page<DealStageHistory> history(@PathVariable UUID id, Pageable pageable) {
        return dealService.getStageHistory(id, pageable);
    }

    @PostMapping("/{id}/stages")
    public ResponseEntity<Deal> changeStage(@PathVariable UUID id, @RequestBody StageChangeRequest req,
                                            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        try {
            DealStage stage = DealStage.valueOf(req.newStage);
            Deal updated = dealService.changeStage(id, stage, req.note, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DealController.class).error("Stage change failed for deal {}", id, e);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Stage change failed",
                e
            );
        }
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> timeline(@PathVariable UUID id){
        try {
            Deal deal = dealRepository.findByIdSafe(id).orElse(null);
            if (deal == null) return ResponseEntity.notFound().build();
            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();

            // Stage changes
            dealService.getStageHistory(id, org.springframework.data.domain.PageRequest.of(0, 200)).forEach(hist -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("type", "STAGE_CHANGE");
                m.put("at", hist.getChangedAt());
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("from", hist.getPreviousStage()!=null?hist.getPreviousStage().name():null);
                data.put("to", hist.getNewStage()!=null?hist.getNewStage().name():null);
                data.put("note", hist.getNote());
                m.put("data", data);
                items.add(m);
            });

            // Notes
            noteRepository.findByDealIdCompatOrderByCreatedAtDesc(id.toString()).forEach(note -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("type", "NOTE");
                m.put("at", note.getCreatedAt());
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("title", note.getTitle());
                data.put("body", note.getBody());
                m.put("data", data);
                items.add(m);
            });

            // Emails
            emailRecordRepository.findByDealOrderBySentAtDesc(deal).forEach(rec -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("type", "EMAIL");
                m.put("at", rec.getSentAt());
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("subject", rec.getSubject());
                m.put("data", data);
                items.add(m);
            });

            // Activities (tasks/events/calls all in Activity)
            activityRepository.findByDealIdCompatAndTypeOrderByCreatedAtDesc(id.toString(), null).forEach(act -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("type", act.getType()!=null?act.getType().name():"ACTIVITY");
                m.put("at", act.getCreatedAt());
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("name", act.getName());
                data.put("status", act.getStatus()!=null?act.getStatus().name():null);
                m.put("data", data);
                items.add(m);
            });

            // sort by time desc (some sources already ordered)
            items.sort((a, b) -> {
                OffsetDateTime atB = asOffsetDateTime(b.get("at"));
                OffsetDateTime atA = asOffsetDateTime(a.get("at"));
                return atB.compareTo(atA);
            });
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DealController.class).error("Timeline failed for deal {}", id, e);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Timeline load failed",
                e
            );
        }
    }

    private static OffsetDateTime asOffsetDateTime(Object value) {
        if (value == null) return OffsetDateTime.MIN;
        if (value instanceof OffsetDateTime odt) return odt;
        if (value instanceof LocalDateTime ldt) return ldt.atOffset(ZoneOffset.UTC);
        // fallback: try parse, but keep safe
        try {
            return OffsetDateTime.parse(String.valueOf(value));
        } catch (Exception ignored) {
            return OffsetDateTime.MIN;
        }
    }
}
