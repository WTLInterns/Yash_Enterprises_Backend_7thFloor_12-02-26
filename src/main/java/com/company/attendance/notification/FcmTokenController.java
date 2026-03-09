package com.company.attendance.notification;

import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fcm-tokens")
@RequiredArgsConstructor
@Slf4j
public class FcmTokenController {

    private final EmployeeFcmTokenRepository fcmTokenRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveToken(@RequestBody Map<String, String> request) {
        try {
            Long employeeId = Long.valueOf(request.get("employeeId"));
            String token = request.get("token");
            String platform = request.getOrDefault("platform", "WEB");
            String deviceId = request.get("deviceId");

            // Validate employee exists
            Optional<Employee> emp = employeeRepository.findById(employeeId);
            if (emp.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employee not found"));
            }

            // Check if token already exists for this employee
            Optional<EmployeeFcmToken> existing = fcmTokenRepository.findByEmployeeIdAndToken(employeeId, token);
            
            if (existing.isPresent()) {
                log.info("Token already exists for employeeId={}, tokenId={}", employeeId, existing.get().getId());
                return ResponseEntity.ok(Map.of("message", "Token already exists", "tokenId", String.valueOf(existing.get().getId())));
            }

            // Save new token
            EmployeeFcmToken newToken = EmployeeFcmToken.builder()
                    .employeeId(employeeId)
                    .token(token)
                    .platform(platform)
                    .deviceId(deviceId)
                    .build();

            EmployeeFcmToken saved = fcmTokenRepository.save(newToken);
            log.info("New FCM token saved for employeeId={}, platform={}, tokenId={}", employeeId, platform, saved.getId());

            return ResponseEntity.ok(Map.of(
                "message", "Token saved successfully",
                "tokenId", String.valueOf(saved.getId())
            ));

        } catch (Exception e) {
            log.error("Failed to save FCM token: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save token"));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeFcmToken>> getEmployeeTokens(@PathVariable Long employeeId) {
        List<EmployeeFcmToken> tokens = fcmTokenRepository.findByEmployeeId(employeeId);
        return ResponseEntity.ok(tokens);
    }

    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Map<String, String>> deleteToken(@PathVariable Long tokenId) {
        try {
            Optional<EmployeeFcmToken> token = fcmTokenRepository.findById(tokenId);
            if (token.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            fcmTokenRepository.delete(token.get());
            log.info("FCM token deleted: tokenId={}", tokenId);
            
            return ResponseEntity.ok(Map.of("message", "Token deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete FCM token: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete token"));
        }
    }

    @DeleteMapping("/employee/{employeeId}/platform/{platform}")
    public ResponseEntity<Map<String, String>> clearEmployeePlatformTokens(
            @PathVariable Long employeeId, 
            @PathVariable String platform) {
        try {
            fcmTokenRepository.deleteByEmployeeIdAndPlatform(employeeId, platform.toUpperCase());
            log.info("Cleared all {} tokens for employeeId={}", platform, employeeId);
            
            return ResponseEntity.ok(Map.of("message", "Tokens cleared successfully"));
        } catch (Exception e) {
            log.error("Failed to clear tokens: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to clear tokens"));
        }
    }
}
