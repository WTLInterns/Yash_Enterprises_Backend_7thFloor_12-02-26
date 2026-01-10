package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.EmailRecord;
import com.company.attendance.crm.service.EmailRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals/{dealId}/emails")
public class EmailRecordController {
    private final EmailRecordService emailService;

    public EmailRecordController(EmailRecordService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public List<EmailRecord> list(@PathVariable UUID dealId){
        return emailService.list(dealId);
    }

    @PostMapping("/send")
    public ResponseEntity<EmailRecord> send(@PathVariable UUID dealId,
                                            @RequestBody EmailRecord record,
                                            @RequestHeader(value = "X-User-Id", required = false) UUID userId){
        EmailRecord sent = emailService.send(dealId, record, userId);
        return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/emails/"+sent.getId())).body(sent);
    }
}
