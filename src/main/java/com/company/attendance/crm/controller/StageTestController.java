package com.company.attendance.crm.controller;

import com.company.attendance.crm.service.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 🧪 Test Controller for Stage Service
 * Used to verify final stage detection and department transitions
 */
@RestController
@RequestMapping("/api/test/stages")
public class StageTestController {

    @Autowired
    private StageService stageService;

    /**
     * 🧪 Test if a stage is final for its department
     */
    @GetMapping("/is-final/{department}/{stageCode}")
    public ResponseEntity<Map<String, Object>> testIsFinalStage(
            @PathVariable String department,
            @PathVariable String stageCode) {
        
        boolean isFinal = stageService.isFinalStage(department, stageCode);
        boolean shouldTransition = stageService.shouldTransitionToAccount(department, stageCode);
        
        Map<String, Object> response = new HashMap<>();
        response.put("department", department);
        response.put("stageCode", stageCode);
        response.put("isFinalStage", isFinal);
        response.put("shouldTransitionToAccount", shouldTransition);
        response.put("firstAccountStage", stageService.getFirstAccountStage());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 🧪 Get all final stages for a department
     */
    @GetMapping("/final-stages/{department}")
    public ResponseEntity<Map<String, Object>> getFinalStages(@PathVariable String department) {
        List<String> finalStages = stageService.getFinalStages(department);
        
        Map<String, Object> response = new HashMap<>();
        response.put("department", department);
        response.put("finalStages", finalStages);
        response.put("firstAccountStage", stageService.getFirstAccountStage());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 🧪 Test all configured final stages
     */
    @GetMapping("/all-final-stages")
    public ResponseEntity<Map<String, Object>> getAllFinalStages() {
        Map<String, Object> response = new HashMap<>();
        response.put("PPO", stageService.getFinalStages("PPO"));
        response.put("PPE", stageService.getFinalStages("PPE"));
        response.put("HCL", stageService.getFinalStages("HCL"));
        response.put("TTL", stageService.getFinalStages("TTL"));
        response.put("ACCOUNT", stageService.getFinalStages("ACCOUNT"));
        response.put("firstAccountStage", stageService.getFirstAccountStage());
        
        return ResponseEntity.ok(response);
    }
}
