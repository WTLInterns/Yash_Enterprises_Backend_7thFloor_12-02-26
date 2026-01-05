package com.company.attendance.mapper;

import com.company.attendance.dto.TeamDto;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.Team;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class TeamMapperImpl implements TeamMapper {

    @Override
    public TeamDto toDto(Team team) {
        if ( team == null ) {
            return null;
        }

        TeamDto.TeamDtoBuilder teamDto = TeamDto.builder();

        teamDto.teamLeadId( teamTeamLeadId( team ) );
        teamDto.parentTeamId( teamParentTeamId( team ) );
        teamDto.memberIds( membersToMemberIds( team.getMembers() ) );
        teamDto.teamLeadName( teamLeadToName( team.getTeamLead() ) );
        teamDto.memberCount( membersToCount( team.getMembers() ) );
        teamDto.parentTeamName( parentTeamToName( team.getParentTeam() ) );
        teamDto.address( team.getAddress() );
        teamDto.city( team.getCity() );
        teamDto.description( team.getDescription() );
        teamDto.id( team.getId() );
        teamDto.isActive( team.getIsActive() );
        teamDto.name( team.getName() );
        teamDto.pincode( team.getPincode() );
        teamDto.state( team.getState() );

        return teamDto.build();
    }

    @Override
    public Team toEntity(TeamDto dto) {
        if ( dto == null ) {
            return null;
        }

        Team.TeamBuilder team = Team.builder();

        team.address( dto.getAddress() );
        team.city( dto.getCity() );
        team.description( dto.getDescription() );
        team.id( dto.getId() );
        team.isActive( dto.getIsActive() );
        team.name( dto.getName() );
        team.pincode( dto.getPincode() );
        team.state( dto.getState() );

        return team.build();
    }

    private Long teamTeamLeadId(Team team) {
        if ( team == null ) {
            return null;
        }
        Employee teamLead = team.getTeamLead();
        if ( teamLead == null ) {
            return null;
        }
        Long id = teamLead.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long teamParentTeamId(Team team) {
        if ( team == null ) {
            return null;
        }
        Team parentTeam = team.getParentTeam();
        if ( parentTeam == null ) {
            return null;
        }
        Long id = parentTeam.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
