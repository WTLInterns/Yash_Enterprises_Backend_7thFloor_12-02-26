package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.Note;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoteService {
    private final DealRepository dealRepository;
    private final NoteRepository noteRepository;

    public NoteService(DealRepository dealRepository, NoteRepository noteRepository) {
        this.dealRepository = dealRepository;
        this.noteRepository = noteRepository;
    }

    public Page<Note> list(Integer dealId, Pageable pageable){
        Deal deal = dealRepository.findByIdSafe(dealId);
        return noteRepository.findByDealOrderByCreatedAtDesc(deal, pageable);
    }

    public Note create(Integer dealId, Note note, Integer userId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        note.setDeal(deal);
        note.setCreatedBy(userId);
        return noteRepository.save(note);
    }

    public Note update(Integer dealId, Integer noteId, Note incoming){
        Note db = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("Note not found"));
        if (!db.getDeal().getId().equals(dealId)) throw new IllegalArgumentException("Note not in deal");
        db.setTitle(incoming.getTitle());
        db.setBody(incoming.getBody());
        return noteRepository.save(db);
    }

    public void delete(Integer dealId, Integer noteId){
        Note db = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("Note not found"));
        if (!db.getDeal().getId().equals(dealId)) throw new IllegalArgumentException("Note not in deal");
        noteRepository.delete(db);
    }
}
