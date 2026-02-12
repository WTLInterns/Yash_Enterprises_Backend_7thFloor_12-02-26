package com.company.attendance.crm.controller;

import com.company.attendance.crm.dto.NoteDto;
import com.company.attendance.crm.entity.Note;
import com.company.attendance.crm.service.NoteService;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deals/{dealId}/notes")
public class NoteController {
    private final NoteService noteService;
    private final EmployeeRepository employeeRepository;

    public NoteController(NoteService noteService, EmployeeRepository employeeRepository) {
        this.noteService = noteService;
        this.employeeRepository = employeeRepository;
    }

    private String employeeName(Long employeeId) {
        if (employeeId == null) return "System";
        Optional<Employee> emp = employeeRepository.findById(employeeId);
        if (emp.isEmpty()) return "System";
        String first = emp.get().getFirstName() != null ? emp.get().getFirstName().trim() : "";
        String last = emp.get().getLastName() != null ? emp.get().getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "System" : full;
    }

    private NoteDto toDto(Note n) {
        NoteDto dto = new NoteDto();
        dto.setId(n.getId());
        dto.setDealId(n.getDeal() != null ? n.getDeal().getId() : null);
        dto.setTitle(n.getTitle());
        dto.setBody(n.getBody());
        dto.setCreatedBy(n.getCreatedBy());
        dto.setCreatedByName(employeeName(n.getCreatedBy()));
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }

    @GetMapping
    public Page<NoteDto> list(@PathVariable Long dealId, Pageable pageable) {
        Page<Note> page = noteService.list(dealId, pageable);
        List<NoteDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @PostMapping
    public ResponseEntity<NoteDto> create(@PathVariable Long dealId, @RequestBody Map<String, Object> body,
                                       @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        Note note = new Note();
        Object title = body.get("title");
        Object text = body.get("text");
        Object noteBody = body.get("body");
        note.setTitle(title != null ? String.valueOf(title) : null);
        note.setBody(noteBody != null ? String.valueOf(noteBody) : (text != null ? String.valueOf(text) : null));
        Note created = noteService.create(dealId, note, userId != null ? userId.longValue() : null);
        return ResponseEntity.created(URI.create("/api/deals/"+dealId+"/notes/"+created.getId())).body(toDto(created));
    }

    @PutMapping("/{noteId}")
    public NoteDto update(@PathVariable Long dealId, @PathVariable Long noteId, @RequestBody Note note) {
        return toDto(noteService.update(noteId, note));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable Long dealId, @PathVariable Long noteId) {
        noteService.delete(noteId);
        return ResponseEntity.noContent().build();
    }
}
