package com.company.attendance.repository;

import com.company.attendance.entity.TaskCustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCustomFieldValueRepository extends JpaRepository<TaskCustomFieldValue, Long> {
    List<TaskCustomFieldValue> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
