package com.company.attendance.crm.repository;

import com.company.attendance.crm.entity.Note;
import com.company.attendance.crm.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByDealOrderByCreatedAtDesc(Deal deal, Pageable pageable);
}
