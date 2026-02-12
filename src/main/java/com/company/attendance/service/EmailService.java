package com.company.attendance.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
        logger.info("Email sent successfully to: {}", to);
    }
    
    public void sendEmployeeEmail(String to, String employeeName, String employeeCode, String organization) {
        try {
            String subject = "Employee Details - " + employeeName;

            String htmlBody =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px;'>" +
                "<h1 style='color: white; margin: 0; font-size: 32px;'>Yashraj Enterprises</h1>" +
                "<p style='color: rgba(255,255,255,0.9); margin: 10px 0 0 0; font-size: 16px;'>Employee Management System</p>" +
                "</div>" +

                "<div style='background: #f8f9fa; padding: 30px; border-radius: 10px; border-left: 4px solid #667eea;'>" +
                "<h2 style='color: #333; margin-top: 0;'>Employee Information</h2>" +

                "<div style='margin: 20px 0;'>" +
                "<p style='margin: 10px 0;'><strong>Name:</strong> <span style='color: #666;'>" + employeeName + "</span></p>" +
                "<p style='margin: 10px 0;'><strong>Employee Code:</strong> <span style='color: #666;'>" + employeeCode + "</span></p>" +
                "<p style='margin: 10px 0;'><strong>Organization:</strong> <span style='color: #666;'>" + organization + "</span></p>" +
                "</div>" +
                "</div>" +

                "<div style='margin: 30px 0; padding: 20px; background: #e8f4fd; border-radius: 10px; text-align: center;'>" +
                "<p style='margin: 0; color: #0066cc; font-size: 14px;'>This is an automated email from Yashraj Enterprises Employee Management System.</p>" +
                "</div>" +

                "<div style='text-align: center; margin-top: 30px; color: #999; font-size: 12px;'>" +
                "<p>&copy; 2026 Yashraj Enterprises. All rights reserved.</p>" +
                "</div>" +
                "</div>";

            sendEmail(to, subject, htmlBody);

            logger.info("Employee details email sent successfully to: {}", to);

        } catch (MessagingException e) {
            logger.error("Error sending employee email to: {}", to, e);
            throw new RuntimeException("Failed to send employee email", e);
        }
    }
    
    public void sendLoginDetailsEmail(String to, String employeeName, String organization, String username, String password) {
        try {
            String subject = "Login Credentials - " + organization;

            String htmlBody =
                "<div style='font-family: Arial, sans-serif; max-width: 650px; margin: 0 auto; padding: 24px;'>" +

                "<div style='background: linear-gradient(135deg, #4f46e5 0%%, #7c3aed 100%%); padding: 26px; border-radius: 14px; text-align:center;'>" +
                "<h1 style='color:#fff; margin:0; font-size:24px;'>Yash Enterprises</h1>" +
                "<p style='color:rgba(255,255,255,0.9); margin:8px 0 0; font-size:14px;'>Employee Login Details</p>" +
                "</div>" +

                "<div style='margin-top:18px; border:1px solid #e5e7eb; border-radius:14px; padding:20px; background:#ffffff;'>" +
                "<p style='margin:0; font-size:15px; color:#111827;'>Hello <b>" + employeeName + "</b>,</p>" +
                "<p style='margin:12px 0 0; font-size:14px; color:#374151; line-height:1.6;'>Your account has been created successfully. Please use below credentials to login.</p>" +

                "<div style='margin-top:16px; background:#f9fafb; border:1px solid #e5e7eb; border-radius:12px; padding:16px;'>" +
                "<p style='margin:8px 0; font-size:14px; color:#111827;'><b>Organization:</b> " + organization + "</p>" +
                "<p style='margin:8px 0; font-size:14px; color:#111827;'><b>Username:</b> " + username + "</p>" +
                "<p style='margin:8px 0; font-size:14px; color:#111827;'><b>Password:</b> " + password + "</p>" +
                "</div>" +

                "<div style='margin-top:16px; background:#eff6ff; border:1px solid #bfdbfe; border-radius:12px; padding:14px;'>" +
                "<p style='margin:0; font-size:13px; color:#1d4ed8;'>For security reasons, please change password after first login.</p>" +
                "</div>" +

                "</div>" +

                "<div style='text-align:center; margin-top:16px; color:#6b7280; font-size:12px;'>" +
                "<p style='margin:0;'>This is an automated mail, do not reply.</p>" +
                "<p style='margin:6px 0 0;'>© 2026 Yash Enterprises</p>" +
                "</div>" +

                "</div>";

            sendEmail(to, subject, htmlBody);

        } catch (MessagingException e) {
            logger.error("Error sending login details email to: {}", to, e);
            throw new RuntimeException("Failed to send login details email", e);
        }
    }
    
    // 📧 Send invoice email with PDF attachment
    public void sendInvoiceEmail(
            String toEmail,
            String customerName,
            String invoiceNo,
            byte[] pdfBytes
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Invoice " + invoiceNo);
            helper.setText(
                    "Dear " + customerName + ",\n\n" +
                    "Please find attached your invoice.\n\n" +
                    "Regards,\nYash Enterprises"
            );

            helper.addAttachment(
                    "Invoice_" + invoiceNo + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send invoice email", e);
        }
    }
}

