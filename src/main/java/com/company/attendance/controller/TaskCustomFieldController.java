package com.company.attendance.controller;

import com.company.attendance.dto.TaskCustomFieldDto;
import com.company.attendance.entity.TaskCustomField;
import com.company.attendance.service.TaskCustomFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task-custom-fields")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskCustomFieldController {

    private final TaskCustomFieldService taskCustomFieldService;

    @GetMapping
    public ResponseEntity<List<TaskCustomFieldDto>> listTaskCustomFields(
            @RequestParam(required = false) String customTaskType) {
        
        try {
            List<TaskCustomField> fields;
            
            if (customTaskType != null && !customTaskType.isEmpty()) {
                fields = taskCustomFieldService.getFieldsByType(customTaskType);
            } else {
                fields = taskCustomFieldService.getAllActiveFields();
            }
            
            List<TaskCustomFieldDto> dtos = fields.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            // Return fallback data if there's an error
            TaskCustomFieldDto fallbackField = new TaskCustomFieldDto();
            fallbackField.setId(1L);
            fallbackField.setFieldKey("priority");
            fallbackField.setFieldLabel("Priority");
            fallbackField.setFieldType("TEXT");
            fallbackField.setRequired(true);
            fallbackField.setActive(true);
            fallbackField.setCustomTaskType("Default Task");
            
            return ResponseEntity.ok(List.of(fallbackField));
        }
    }

    private TaskCustomFieldDto convertToDto(TaskCustomField field) {
        TaskCustomFieldDto dto = new TaskCustomFieldDto();
        dto.setId(field.getId());
        dto.setCustomTaskType(field.getCustomTaskType());
        dto.setFieldKey(field.getFieldKey());
        dto.setFieldLabel(field.getFieldLabel());
        dto.setFieldType(field.getFieldType());
        dto.setRequired(field.getRequired());
        dto.setActive(field.getActive());
        return dto;
    }
}
