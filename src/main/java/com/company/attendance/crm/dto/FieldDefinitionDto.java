package com.company.attendance.crm.dto;

import com.company.attendance.crm.entity.FieldDefinition;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

@Data
public class FieldDefinitionDto {
    private Long id;
    private String entity;
    private String fieldKey;
    private String fieldName;
    private FieldDefinition.FieldType fieldType;
    private Boolean required;
    private String optionsJson;
    private Boolean active;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant updatedAt;
    
    private Integer createdBy;
    private Integer updatedBy;
}
