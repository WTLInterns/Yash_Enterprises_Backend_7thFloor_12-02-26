package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.StageMaster;
import com.company.attendance.crm.repository.StageMasterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/stages")
public class StageController {
    
    private final StageMasterRepository stageMasterRepository;
    
    public StageController(StageMasterRepository stageMasterRepository) {
        this.stageMasterRepository = stageMasterRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllStages(
            @RequestParam(required = false) String department) {
        
        List<StageMaster> stages;
        if (department != null && !department.isBlank()) {
            stages = stageMasterRepository.findByDepartmentOrderByStageOrder(department);
        } else {
            stages = stageMasterRepository.findAll();
        }
        
        List<Map<String, Object>> result = stages.stream().map(stage -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", stage.getId());
            map.put("department", stage.getDepartment());
            map.put("stageCode", stage.getStageCode());
            map.put("stageName", stage.getStageName());
            map.put("stageOrder", stage.getStageOrder());
            map.put("isTerminal", stage.getIsTerminal());
            return map;
        }).toList();
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = stageMasterRepository.findAllDepartments();
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/{department}")
    public ResponseEntity<List<Map<String, Object>>> getStagesByDepartment(
            @PathVariable String department) {
        
        List<StageMaster> stages = stageMasterRepository.findByDepartmentOrderByStageOrder(department);
        
        List<Map<String, Object>> result = stages.stream().map(stage -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", stage.getId());
            map.put("department", stage.getDepartment());
            map.put("stageCode", stage.getStageCode());
            map.put("stageName", stage.getStageName());
            map.put("stageOrder", stage.getStageOrder());
            map.put("isTerminal", stage.getIsTerminal());
            return map;
        }).toList();
        
        return ResponseEntity.ok(result);
    }
}
