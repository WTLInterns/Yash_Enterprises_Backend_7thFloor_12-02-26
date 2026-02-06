package com.company.attendance.mapper;

import com.company.attendance.dto.TaskCustomFieldValueDto;
import com.company.attendance.dto.TaskDto;
import com.company.attendance.entity.Task;
import com.company.attendance.entity.TaskCustomFieldValue;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "assignedToEmployeeName", ignore = true)
    @Mapping(target = "createdByEmployeeName", ignore = true)
    @Mapping(target = "customFieldValues", ignore = true)
    TaskDto toDto(Task task);

    @Mapping(target = "customFieldValues", ignore = true)
    Task toEntity(TaskDto dto);

    // custom fields mapping
    @Mapping(target = "fieldId", source = "field.id")
    @Mapping(target = "fieldKey", source = "field.fieldKey")
    @Mapping(target = "fieldLabel", source = "field.fieldLabel")
    @Mapping(target = "fieldType", source = "field.fieldType")
    @Mapping(target = "required", source = "field.required")
    TaskCustomFieldValueDto toCustomFieldValueDto(TaskCustomFieldValue value);

    List<TaskCustomFieldValueDto> toCustomFieldValueDtoList(List<TaskCustomFieldValue> values);
}

