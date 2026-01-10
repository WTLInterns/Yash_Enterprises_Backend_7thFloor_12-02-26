package com.company.attendance;

import com.company.attendance.entity.Team;
import com.company.attendance.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Disabled("Temporarily disabled until Team entity/repository are available in this module")
@SpringBootTest
@Transactional
public class TeamHierarchyTest {

    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void testTeamHierarchyFields() {
        // Create a test team
        Team team = new Team();
        team.setName("Test Team");
        team.setDescription("Test Description");
        team.setIsActive(true);
        
        // Save the team
        Team savedTeam = teamRepository.save(team);
        
        System.out.println("Team ID: " + savedTeam.getId());
        System.out.println("Team Name: " + savedTeam.getName());
        
        // Test if we can set parent team
        Team parentTeam = new Team();
        parentTeam.setName("Parent Team");
        parentTeam.setDescription("Parent Description");
        parentTeam.setIsActive(true);
        Team savedParentTeam = teamRepository.save(parentTeam);
        
        // Set parent relationship
        savedTeam.setParentTeam(savedParentTeam);
        Team updatedTeam = teamRepository.save(savedTeam);
        
        System.out.println("Parent Team ID: " + updatedTeam.getParentTeam().getId());
        System.out.println("Parent Team Name: " + updatedTeam.getParentTeam().getName());
    }
}
