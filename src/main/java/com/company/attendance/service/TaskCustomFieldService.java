package com.company.attendance.service;

import com.company.attendance.dto.TaskCustomFieldDto;
import com.company.attendance.entity.TaskCustomField;
import com.company.attendance.repository.TaskCustomFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCustomFieldService {

    private final TaskCustomFieldRepository repository;

    public List<TaskCustomField> getFieldsByType(String customTaskType) {
        return repository.findByCustomTaskTypeAndActiveTrueOrderBySortOrderAsc(customTaskType);
    }

    public List<TaskCustomField> getAllActiveFields() {
        return repository.findByActiveTrueOrderBySortOrderAsc();
    }

    public TaskCustomField save(TaskCustomField field) {
        return repository.save(field);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
