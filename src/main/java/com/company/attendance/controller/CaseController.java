package com.company.attendance.controller;

import com.company.attendance.dto.CaseDto;
import com.company.attendance.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CaseController {
    
    private final CaseService caseService;
    
    @PostMapping
    public ResponseEntity<CaseDto> createCase(@Valid @RequestBody CaseDto caseDto) {
        try {
            CaseDto createdCase = caseService.createCase(caseDto);
            return new ResponseEntity<>(createdCase, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CaseDto> updateCase(@PathVariable Long id, @Valid @RequestBody CaseDto caseDto) {
        try {
            CaseDto updatedCase = caseService.updateCase(id, caseDto);
            return new ResponseEntity<>(updatedCase, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CaseDto> getCaseById(@PathVariable Long id) {
        try {
            CaseDto caseDto = caseService.getCaseById(id);
            return new ResponseEntity<>(caseDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<CaseDto>> getAllCases() {
        try {
            List<CaseDto> cases = caseService.getAllCases();
            return new ResponseEntity<>(cases, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CaseDto>> getCasesByClientId(@PathVariable Long clientId) {
        try {
            List<CaseDto> cases = caseService.getCasesByClientId(clientId);
            return new ResponseEntity<>(cases, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CaseDto>> searchCases(@RequestParam String search) {
        try {
            List<CaseDto> cases = caseService.searchCases(search);
            return new ResponseEntity<>(cases, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id) {
        try {
            caseService.deleteCase(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
