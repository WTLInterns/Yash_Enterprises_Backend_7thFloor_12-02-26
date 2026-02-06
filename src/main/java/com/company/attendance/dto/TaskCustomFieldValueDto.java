package com.company.attendance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCustomFieldValueDto {
    private Long taskCustomFieldId;
    private Long fieldId;
    private String fieldKey;
    private String fieldLabel;
    private String fieldType;
    private Boolean required;
    private String value;
}
