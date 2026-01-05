package com.company.attendance.mapper;

import com.company.attendance.dto.SupportTicketDto;
import com.company.attendance.entity.SupportTicket;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class SupportTicketMapperImpl implements SupportTicketMapper {

    @Override
    public SupportTicketDto toDto(SupportTicket entity) {
        if ( entity == null ) {
            return null;
        }

        SupportTicketDto supportTicketDto = new SupportTicketDto();

        supportTicketDto.setPriority( entity.getPriority() );
        supportTicketDto.setStatus( entity.getStatus() );
        supportTicketDto.setAssignedTo( entity.getAssignedTo() );
        supportTicketDto.setCategory( entity.getCategory() );
        supportTicketDto.setCreatedAt( entity.getCreatedAt() );
        supportTicketDto.setCreatedBy( entity.getCreatedBy() );
        supportTicketDto.setDescription( entity.getDescription() );
        supportTicketDto.setId( entity.getId() );
        supportTicketDto.setResolution( entity.getResolution() );
        supportTicketDto.setSubject( entity.getSubject() );
        supportTicketDto.setUpdatedAt( entity.getUpdatedAt() );

        return supportTicketDto;
    }

    @Override
    public SupportTicket toEntity(SupportTicketDto dto) {
        if ( dto == null ) {
            return null;
        }

        SupportTicket.SupportTicketBuilder supportTicket = SupportTicket.builder();

        supportTicket.priority( dto.getPriority() );
        supportTicket.status( dto.getStatus() );
        supportTicket.assignedTo( dto.getAssignedTo() );
        supportTicket.category( dto.getCategory() );
        supportTicket.createdBy( dto.getCreatedBy() );
        supportTicket.description( dto.getDescription() );
        supportTicket.resolution( dto.getResolution() );
        supportTicket.subject( dto.getSubject() );

        return supportTicket.build();
    }

    @Override
    public void updateEntityFromDto(SupportTicketDto dto, SupportTicket entity) {
        if ( dto == null ) {
            return;
        }

        entity.setPriority( dto.getPriority() );
        entity.setStatus( dto.getStatus() );
        entity.setAssignedTo( dto.getAssignedTo() );
        entity.setCategory( dto.getCategory() );
        entity.setCreatedAt( dto.getCreatedAt() );
        entity.setCreatedBy( dto.getCreatedBy() );
        entity.setDescription( dto.getDescription() );
        entity.setId( dto.getId() );
        entity.setResolution( dto.getResolution() );
        entity.setSubject( dto.getSubject() );
        entity.setUpdatedAt( dto.getUpdatedAt() );
    }
}
