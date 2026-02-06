package com.company.attendance.repository;

import com.company.attendance.entity.TaskCustomField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCustomFieldRepository extends JpaRepository<TaskCustomField, Long> {
    List<TaskCustomField> findByActiveTrueAndCustomTaskType(String customTaskType);
    List<TaskCustomField> findByCustomTaskTypeAndActiveTrueOrderBySortOrderAsc(String customTaskType);
    List<TaskCustomField> findByCustomTaskTypeOrderBySortOrderAsc(String customTaskType);
    List<TaskCustomField> findByActiveTrueOrderBySortOrderAsc();
}
