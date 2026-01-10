package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.FileMeta;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FileMetaRepository extends JpaRepository<FileMeta, UUID> {
    List<FileMeta> findByDeal(Deal deal);
}
