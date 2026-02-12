package com.company.attendance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class DatabaseFixController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/table-structure")
    public Map<String, Object> getTableStructure() {
        try {
            String sql = "DESCRIBE customer_addresses";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            log.info("Table structure retrieved");
            return Map.of("success", true, "data", result);
        } catch (Exception e) {
            log.error("Failed to get table structure", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @PostMapping("/fix-column-name")
    public String fixColumnName() {
        try {
            // Drop the duplicate address_line column first
            String dropLineSql = "ALTER TABLE customer_addresses DROP COLUMN address_line";
            jdbcTemplate.execute(dropLineSql);
            log.info("Dropped duplicate address_line column");
            
            // Rename address_text to address_line
            String renameSql = "ALTER TABLE customer_addresses CHANGE address_text address_line VARCHAR(255) NOT NULL";
            jdbcTemplate.execute(renameSql);
            log.info("Renamed address_text to address_line");
            
            return "Database column 'address_text' renamed to 'address_line' successfully!";
        } catch (Exception e) {
            log.error("Failed to fix column name", e);
            return "Error: " + e.getMessage();
        }
    }
}
