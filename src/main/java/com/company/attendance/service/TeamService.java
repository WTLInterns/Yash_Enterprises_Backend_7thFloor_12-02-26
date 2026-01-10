package com.company.attendance.service;

import com.company.attendance.entity.Team;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.TeamRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.dto.TeamDto;
import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.mapper.TeamMapper;
import com.company.attendance.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamMapper teamMapper;

    public List<TeamDto> findAll() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
            .map(teamMapper::toDto)
            .toList();
    }

    public TeamDto getById(Long id) {
        Team team = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        return teamMapper.toDto(team);
    }

    @Transactional
    public TeamDto create(TeamDto teamDto) {
        Team team = teamMapper.toEntity(teamDto);
        
        // Set team lead if specified
        if (teamDto.getTeamLeadId() != null) {
            Employee teamLead = employeeRepository.findById(teamDto.getTeamLeadId())
                .orElseThrow(() -> new RuntimeException("Team lead not found with id: " + teamDto.getTeamLeadId()));
            team.setTeamLead(teamLead);
        }
        
        // Set parent team if specified
        if (teamDto.getParentTeamId() != null) {
            Team parentTeam = teamRepository.findById(teamDto.getParentTeamId())
                .orElseThrow(() -> new RuntimeException("Parent team not found with id: " + teamDto.getParentTeamId()));
            team.setParentTeam(parentTeam);
        }
        
        // Set team members if specified
        if (teamDto.getMemberIds() != null && !teamDto.getMemberIds().isEmpty()) {
            Set<Employee> members = new HashSet<>(employeeRepository.findAllById(teamDto.getMemberIds()));
            team.setMembers(members);
        }
        
        Team savedTeam = teamRepository.save(team);
        return teamMapper.toDto(savedTeam);
    }

    @Transactional
    public TeamDto update(Long id, TeamDto teamDto) {
        Team existingTeam = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        
        // Update basic fields
        existingTeam.setName(teamDto.getName());
        existingTeam.setDescription(teamDto.getDescription());
        existingTeam.setAddress(teamDto.getAddress());
        existingTeam.setCity(teamDto.getCity());
        existingTeam.setState(teamDto.getState());
        existingTeam.setPincode(teamDto.getPincode());
        existingTeam.setIsActive(teamDto.getIsActive());
        
        // Update team lead if specified
        if (teamDto.getTeamLeadId() != null) {
            Employee teamLead = employeeRepository.findById(teamDto.getTeamLeadId())
                .orElseThrow(() -> new RuntimeException("Team lead not found with id: " + teamDto.getTeamLeadId()));
            existingTeam.setTeamLead(teamLead);
        }
        
        // Update parent team if specified
        if (teamDto.getParentTeamId() != null) {
            // Prevent self-reference
            if (teamDto.getParentTeamId().equals(id)) {
                throw new RuntimeException("Team cannot be its own parent");
            }
            Team parentTeam = teamRepository.findById(teamDto.getParentTeamId())
                .orElseThrow(() -> new RuntimeException("Parent team not found with id: " + teamDto.getParentTeamId()));
            existingTeam.setParentTeam(parentTeam);
        } else {
            existingTeam.setParentTeam(null);
        }
        
        // Update team members if specified
        if (teamDto.getMemberIds() != null) {
            Set<Employee> members = new HashSet<>(employeeRepository.findAllById(teamDto.getMemberIds()));
            existingTeam.setMembers(members);
        }
        
        Team savedTeam = teamRepository.save(existingTeam);
        return teamMapper.toDto(savedTeam);
    }

    @Transactional
    public void assignTeamLead(Long teamId, Long teamLeadId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        Employee teamLead = employeeRepository.findById(teamLeadId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + teamLeadId));
        
        team.setTeamLead(teamLead);
        teamRepository.save(team);
    }

    @Transactional
    public void assignTeamMembers(Long teamId, List<Long> memberIds) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        // Assign new members
        if (memberIds != null && !memberIds.isEmpty()) {
            Set<Employee> members = new HashSet<>(employeeRepository.findAllById(memberIds));
            team.setMembers(members);
        } else {
            team.setMembers(new HashSet<>());
        }
        
        teamRepository.save(team);
    }

    @Transactional
    public void delete(Long id) {
        Team team = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        
        // Check if team has members
        if (team.getMembers() != null && !team.getMembers().isEmpty()) {
            throw new RuntimeException("Cannot delete team with assigned members. Please reassign or remove members first.");
        }
        
        // Check if team has sub-teams
        if (team.getSubTeams() != null && !team.getSubTeams().isEmpty()) {
            throw new RuntimeException("Cannot delete team with sub-teams. Please delete or reassign sub-teams first.");
        }
        
        // Check if any employee has this as their team
        List<Employee> employeesWithTeam = employeeRepository.findByTeamId(id);
        if (employeesWithTeam != null && !employeesWithTeam.isEmpty()) {
            throw new RuntimeException("Cannot delete team. Employees are still assigned to this team. Please reassign employees first.");
        }
        
        teamRepository.deleteById(id);
    }

    public List<EmployeeDto> getTeamMembers(Long teamId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        if (team.getMembers() == null || team.getMembers().isEmpty()) {
            return List.of();
        }
        
        return team.getMembers().stream()
            .map(member -> EmployeeDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .employeeId(member.getEmployeeId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .profileImageUrl(member.getProfileImageUrl())
                .roleName(member.getRole() != null ? member.getRole().getName() : null)
                .teamName(member.getTeam() != null ? member.getTeam().getName() : null)
                .build())
            .toList();
    }
}

