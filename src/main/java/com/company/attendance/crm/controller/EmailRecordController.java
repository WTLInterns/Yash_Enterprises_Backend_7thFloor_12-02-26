package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.EmailRecord;
import com.company.attendance.crm.service.EmailRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/deals/{dealId}/emails")
public class EmailRecordController {
    private final EmailRecordService emailService;

    public EmailRecordController(EmailRecordService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public List<EmailRecord> list(@PathVariable Long dealId) {
        return emailService.list(dealId);
    }

    @PostMapping(value = "/send", consumes = "multipart/form-data")
    public ResponseEntity<EmailRecord> send(
            @PathVariable Long dealId,
            @RequestParam("toAddress") String toAddress,
            @RequestParam(value = "ccAddress", required = false) String ccAddress,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "body", required = false) String body,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        EmailRecord sent = emailService.send(dealId, toAddress, ccAddress, subject, body, userId, attachment);
        return ResponseEntity
                .created(URI.create("/api/deals/" + dealId + "/emails/" + sent.getId()))
                .body(sent);
    }
}
