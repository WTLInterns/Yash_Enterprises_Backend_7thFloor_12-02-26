package com.company.attendance.dto;

import lombok.Data;

@Data
public class TaskCustomFieldDto {
    private Long id;
    private String customTaskType;
    private String fieldKey;
    private String fieldLabel;
    private String fieldType;
    private Boolean required;
    private String options;
    private Integer sortOrder;
    private Boolean active;
}
