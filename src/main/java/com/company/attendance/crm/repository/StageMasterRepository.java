package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.StageMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StageMasterRepository extends JpaRepository<StageMaster, Long> {
    
    @Query("SELECT s FROM StageMaster s WHERE s.department = :department ORDER BY s.stageOrder")
    List<StageMaster> findByDepartmentOrderByStageOrder(@Param("department") String department);
    
    @Query("SELECT s FROM StageMaster s WHERE s.department = :department AND s.stageCode = :stageCode")
    StageMaster findByDepartmentAndStageCode(@Param("department") String department, @Param("stageCode") String stageCode);
    
    @Query("SELECT DISTINCT s.department FROM StageMaster s ORDER BY s.department")
    List<String> findAllDepartments();
}
