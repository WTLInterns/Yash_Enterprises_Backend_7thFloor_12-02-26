package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Note;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    Page<Note> findByDealOrderByCreatedAtDesc(Deal deal, Pageable pageable);

    @Query(value = "select * from notes where substring(deal_id,1,16) = uuid_to_bin(:dealId) order by created_at desc", nativeQuery = true)
    List<Note> findByDealIdCompatOrderByCreatedAtDesc(@Param("dealId") String dealId);
}
