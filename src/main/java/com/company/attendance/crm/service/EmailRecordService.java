package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.EmailRecord;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.EmailRecordRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmailRecordService {
    private final DealRepository dealRepository;
    private final EmailRecordRepository emailRecordRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public EmailRecordService(DealRepository dealRepository,
                              EmailRecordRepository emailRecordRepository,
                              JavaMailSender mailSender) {
        this.dealRepository = dealRepository;
        this.emailRecordRepository = emailRecordRepository;
        this.mailSender = mailSender;
    }

    public List<EmailRecord> list(Long dealId) {
        Deal deal = dealRepository.findByIdSafe(dealId);
        return emailRecordRepository.findByDealOrderBySentAtDesc(deal);
    }

    public EmailRecord send(Long dealId, String toAddress, String ccAddress,
                            String subject, String body, Integer userId,
                            MultipartFile attachment) {
        Deal deal = dealRepository.findByIdSafe(dealId);

        EmailRecord record = new EmailRecord();
        record.setDeal(deal);
        record.setToAddress(toAddress);
        record.setCcAddress(ccAddress);
        record.setSubject(subject);
        record.setBody(body);
        record.setSentBy(userId);
        record.setSentAt(OffsetDateTime.now());

        // Save attachment file if present
        File attachFile = null;
        if (attachment != null && !attachment.isEmpty()) {
            try {
                Path dir = Paths.get(uploadDir, "email-attachments");
                Files.createDirectories(dir);
                String filename = UUID.randomUUID() + "_" + attachment.getOriginalFilename();
                Path dest = dir.resolve(filename);
                attachment.transferTo(dest.toFile());
                record.setAttachmentName(attachment.getOriginalFilename());
                record.setAttachmentPath(dest.toString());
                attachFile = dest.toFile();
            } catch (IOException e) {
                // log but don't fail the send
            }
        }

        // Send actual email
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, attachFile != null, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toAddress);
            if (ccAddress != null && !ccAddress.isBlank()) {
                helper.setCc(ccAddress);
            }
            helper.setSubject(subject != null ? subject : "");
            helper.setText(body != null ? body : "", false);
            if (attachFile != null && attachFile.exists()) {
                helper.addAttachment(record.getAttachmentName(), attachFile);
            }
            mailSender.send(msg);
            record.setStatus("SENT");
        } catch (MessagingException e) {
            record.setStatus("FAILED");
        }

        return emailRecordRepository.save(record);
    }
}
