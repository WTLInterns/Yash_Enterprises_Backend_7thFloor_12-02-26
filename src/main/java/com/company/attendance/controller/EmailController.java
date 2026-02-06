package com.company.attendance.controller;

import com.company.attendance.entity.Employee;
import com.company.attendance.service.EmailService;
import com.company.attendance.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final EmployeeService employeeService;

    @PostMapping("/send-employee-email/{employeeId}")
    public ResponseEntity<Map<String, Object>> sendEmployeeEmail(@PathVariable Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get employee details
            Optional<Employee> employeeOpt = employeeService.findById(employeeId);
            
            if (employeeOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Employee employee = employeeOpt.get();
            
            // Check if employee has email
            if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee email address not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Send email
            String fullName = (employee.getLastName() != null && !employee.getLastName().isEmpty()) 
                ? employee.getFirstName() + " " + employee.getLastName()
                : employee.getFirstName();
                
            emailService.sendEmployeeEmail(
                employee.getEmail(),
                fullName,
                employee.getEmployeeId(),
                "Yashraj Enterprises"
            );
            
            response.put("success", true);
            response.put("message", "Email sent successfully to " + fullName);
            response.put("employeeName", fullName);
            response.put("employeeEmail", employee.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/send-login-details/{employeeId}")
    public ResponseEntity<Map<String, Object>> sendLoginDetails(@PathVariable Long employeeId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Employee> employeeOpt = employeeService.findById(employeeId);

            if (employeeOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }

            Employee employee = employeeOpt.get();

            if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee email not found");
                return ResponseEntity.badRequest().body(response);
            }

            if (employee.getPhone() == null || employee.getPhone().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee phone not found (password is phone)");
                return ResponseEntity.badRequest().body(response);
            }

            String fullName = (employee.getLastName() != null && !employee.getLastName().isBlank())
                    ? employee.getFirstName() + " " + employee.getLastName()
                    : employee.getFirstName();

            String organization = "Yash Enterprises";

            // ✅ Password = phone
            String password = employee.getPhone().trim();

            emailService.sendLoginDetailsEmail(
                    employee.getEmail(),
                    fullName,
                    organization,
                    employee.getEmail(),   // username = email
                    password               // password = phone
            );

            response.put("success", true);
            response.put("message", "Login details email sent successfully");
            response.put("employeeName", fullName);
            response.put("employeeEmail", employee.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/send-custom-email/{employeeId}")
    public ResponseEntity<Map<String, Object>> sendCustomEmail(
            @PathVariable Long employeeId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String customMessage = request.get("message");
            String subject = request.get("subject");
            
            if (customMessage == null || customMessage.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message content is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get employee details
            Optional<Employee> employeeOpt = employeeService.findById(employeeId);
            
            if (employeeOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Employee employee = employeeOpt.get();
            
            if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee email address not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Send custom email
            String fullName = (employee.getLastName() != null && !employee.getLastName().isEmpty()) 
                ? employee.getFirstName() + " " + employee.getLastName()
                : employee.getFirstName();
                
            String emailSubject = subject != null ? subject : "Message from Yashraj Enterprises";
            emailService.sendEmail(employee.getEmail(), emailSubject, customMessage);
            
            response.put("success", true);
            response.put("message", "Custom email sent successfully to " + fullName);
            response.put("employeeName", fullName);
            response.put("employeeEmail", employee.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
