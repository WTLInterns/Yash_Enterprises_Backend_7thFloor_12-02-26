package com.company.attendance.controller;

import com.company.attendance.dto.FormDto;
import com.company.attendance.service.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FormController {

    private final FormService formService;

    @GetMapping
    public ResponseEntity<List<FormDto>> getAllForms() {
        List<FormDto> forms = formService.findAll();
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormDto> getFormById(@PathVariable Long id) {
        FormDto form = formService.findById(id);
        if (form != null) {
            return ResponseEntity.ok(form);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FormDto>> getFormsByClientId(@PathVariable Long clientId) {
        List<FormDto> forms = formService.findByClientId(clientId);
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/created-by/{createdBy}")
    public ResponseEntity<List<FormDto>> getFormsByCreatedBy(@PathVariable Long createdBy) {
        List<FormDto> forms = formService.findByCreatedBy(createdBy);
        return ResponseEntity.ok(forms);
    }

    @PostMapping
    public ResponseEntity<FormDto> createForm(@RequestBody FormDto formDto) {
        // Use createdBy from request body, fallback to authenticated user if needed
        FormDto createdForm = formService.create(formDto, formDto.getCreatedBy());
        return ResponseEntity.ok(createdForm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormDto> updateForm(@PathVariable Long id, @RequestBody FormDto formDto) {
        // Use updatedBy from request body, fallback to authenticated user if needed
        FormDto updatedForm = formService.update(id, formDto, formDto.getUpdatedBy());
        if (updatedForm != null) {
            return ResponseEntity.ok(updatedForm);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        try {
            formService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

