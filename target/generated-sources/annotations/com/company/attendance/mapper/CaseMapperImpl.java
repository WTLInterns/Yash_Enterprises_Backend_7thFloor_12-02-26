package com.company.attendance.mapper;

import com.company.attendance.dto.CaseDto;
import com.company.attendance.entity.Case;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class CaseMapperImpl implements CaseMapper {

    @Override
    public CaseDto toDto(Case caseEntity) {
        if ( caseEntity == null ) {
            return null;
        }

        CaseDto caseDto = new CaseDto();

        caseDto.setCaseNumber( caseEntity.getCaseNumber() );
        caseDto.setCreatedAt( caseEntity.getCreatedAt() );
        caseDto.setDescription( caseEntity.getDescription() );
        caseDto.setId( caseEntity.getId() );
        if ( caseEntity.getPriority() != null ) {
            caseDto.setPriority( caseEntity.getPriority().name() );
        }
        if ( caseEntity.getStatus() != null ) {
            caseDto.setStatus( caseEntity.getStatus().name() );
        }
        caseDto.setTitle( caseEntity.getTitle() );
        caseDto.setUpdatedAt( caseEntity.getUpdatedAt() );

        return caseDto;
    }

    @Override
    public Case toEntity(CaseDto caseDto) {
        if ( caseDto == null ) {
            return null;
        }

        Case.CaseBuilder case1 = Case.builder();

        case1.caseNumber( caseDto.getCaseNumber() );
        case1.createdAt( caseDto.getCreatedAt() );
        case1.description( caseDto.getDescription() );
        case1.id( caseDto.getId() );
        if ( caseDto.getPriority() != null ) {
            case1.priority( Enum.valueOf( Case.Priority.class, caseDto.getPriority() ) );
        }
        if ( caseDto.getStatus() != null ) {
            case1.status( Enum.valueOf( Case.CaseStatus.class, caseDto.getStatus() ) );
        }
        case1.title( caseDto.getTitle() );
        case1.updatedAt( caseDto.getUpdatedAt() );

        return case1.build();
    }

    @Override
    public void updateEntityFromDto(CaseDto caseDto, Case caseEntity) {
        if ( caseDto == null ) {
            return;
        }

        caseEntity.setCaseNumber( caseDto.getCaseNumber() );
        caseEntity.setCreatedAt( caseDto.getCreatedAt() );
        caseEntity.setDescription( caseDto.getDescription() );
        caseEntity.setId( caseDto.getId() );
        if ( caseDto.getPriority() != null ) {
            caseEntity.setPriority( Enum.valueOf( Case.Priority.class, caseDto.getPriority() ) );
        }
        else {
            caseEntity.setPriority( null );
        }
        if ( caseDto.getStatus() != null ) {
            caseEntity.setStatus( Enum.valueOf( Case.CaseStatus.class, caseDto.getStatus() ) );
        }
        else {
            caseEntity.setStatus( null );
        }
        caseEntity.setTitle( caseDto.getTitle() );
        caseEntity.setUpdatedAt( caseDto.getUpdatedAt() );
    }

    @Override
    public List<CaseDto> toDtoList(List<Case> cases) {
        if ( cases == null ) {
            return null;
        }

        List<CaseDto> list = new ArrayList<CaseDto>( cases.size() );
        for ( Case case1 : cases ) {
            list.add( toDto( case1 ) );
        }

        return list;
    }

    @Override
    public List<Case> toEntityList(List<CaseDto> caseDtos) {
        if ( caseDtos == null ) {
            return null;
        }

        List<Case> list = new ArrayList<Case>( caseDtos.size() );
        for ( CaseDto caseDto : caseDtos ) {
            list.add( toEntity( caseDto ) );
        }

        return list;
    }
}
