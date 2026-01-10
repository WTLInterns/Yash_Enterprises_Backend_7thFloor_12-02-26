package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.Note;
import com.company.attendance.crm.service.NoteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals/{dealId}/notes")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public Page<Note> list(@PathVariable UUID dealId, Pageable pageable) {
        return noteService.list(dealId, pageable);
    }

    @PostMapping
    public ResponseEntity<Note> create(@PathVariable UUID dealId, @RequestBody Note note,
                                       @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        Note created = noteService.create(dealId, note, userId);
        return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/notes/"+created.getId())).body(created);
    }

    @PutMapping("/{noteId}")
    public Note update(@PathVariable UUID dealId, @PathVariable UUID noteId, @RequestBody Note note) {
        return noteService.update(dealId, noteId, note);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable UUID dealId, @PathVariable UUID noteId) {
        noteService.delete(dealId, noteId);
        return ResponseEntity.noContent().build();
    }
}
