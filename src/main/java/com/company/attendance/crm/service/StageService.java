package com.company.attendance.crm.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Stage Service - Manages stage transitions and final stage detection
 * 
 * This service handles the automatic department transition logic
 * when deals reach the final stage of their current department.
 */
@Service
public class StageService {

    /**
     * Final stages for each department
     * When a deal reaches any of these stages, it automatically moves to ACCOUNT
     * 
     * UPDATED: "ACCOUNT" is now the trigger stage for all departments
     */
    private static final Map<String, List<String>> FINAL_STAGES = Map.of(
        "PPO", List.of("ACCOUNT"),
        "PPE", List.of("ACCOUNT"), 
        "HCL", List.of("ACCOUNT"),
        "TTL", List.of("ACCOUNT")
        // Note: ACCOUNT has no final stage - it's the final destination
    );

    /**
     * 🥇 First stage of ACCOUNT department
     * This is assigned when a deal moves to ACCOUNT
     */
    private static final String FIRST_ACCOUNT_STAGE = "ACCOUNT_NEW";

    /**
     * 🔍 Check if a stage is the final stage for its department
     * 
     * @param department The department (PPO, PPE, HCL, TTL)
     * @param stageCode The stage code to check
     * @return true if this is a final stage, false otherwise
     */
    public boolean isFinalStage(String department, String stageCode) {
        if (department == null || stageCode == null) {
            return false;
        }
        
        List<String> finalStages = FINAL_STAGES.get(department.toUpperCase());
        return finalStages != null && finalStages.contains(stageCode.toUpperCase());
    }

    /**
     * Get the first stage of ACCOUNT department
     * 
     * @return The first ACCOUNT stage code
     */
    public String getFirstAccountStage() {
        return FIRST_ACCOUNT_STAGE;
    }

    /**
     * Get all final stages for a department
     * 
     * @param department The department
     * @return List of final stage codes (empty if none)
     */
    public List<String> getFinalStages(String department) {
        if (department == null) {
            return List.of();
        }
        
        return FINAL_STAGES.getOrDefault(department.toUpperCase(), List.of());
    }

    /**
     * Check if a department transition should occur
     * 
     * @param currentDepartment Current deal department
     * @param newStageCode New stage being set
     * @return true if deal should move to ACCOUNT
     */
    public boolean shouldTransitionToAccount(String currentDepartment, String newStageCode) {
        return !"ACCOUNT".equalsIgnoreCase(currentDepartment) && 
               isFinalStage(currentDepartment, newStageCode);
    }
}
