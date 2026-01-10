package com.company.attendance.controller;

import com.company.attendance.dto.TeamDto;
import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.service.TeamService;
import com.company.attendance.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeamController {
    private final TeamService teamService;
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<TeamDto>> listTeams() {
        List<TeamDto> teams = teamService.findAll();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeam(@PathVariable Long id) {
        TeamDto team = teamService.getById(id);
        return ResponseEntity.ok(team);
    }

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody TeamDto dto) {
        TeamDto createdTeam = teamService.create(dto);
        return ResponseEntity.ok(createdTeam);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDto> updateTeam(@PathVariable Long id, @Valid @RequestBody TeamDto dto) {
        TeamDto updatedTeam = teamService.update(id, dto);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTeam(@PathVariable Long id) {
        try {
            teamService.delete(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Team deleted successfully"
            ));
        } catch (RuntimeException e) {
            // Return 400 for validation/business logic errors instead of 500
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete team: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{id}/assign-lead")
    public ResponseEntity<Void> assignTeamLead(@PathVariable Long id, @RequestBody Long teamLeadId) {
        teamService.assignTeamLead(id, teamLeadId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/assign-members")
    public ResponseEntity<Void> assignTeamMembers(@PathVariable Long id, @RequestBody List<Long> memberIds) {
        teamService.assignTeamMembers(id, memberIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<EmployeeDto>> getTeamMembers(@PathVariable Long id) {
        List<EmployeeDto> members = teamService.getTeamMembers(id);
        return ResponseEntity.ok(members);
    }
}

