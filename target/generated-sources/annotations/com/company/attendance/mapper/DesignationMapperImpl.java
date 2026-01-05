package com.company.attendance.mapper;

import com.company.attendance.dto.DesignationDto;
import com.company.attendance.entity.Designation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class DesignationMapperImpl implements DesignationMapper {

    @Override
    public Designation toEntity(DesignationDto dto) {
        if ( dto == null ) {
            return null;
        }

        Designation.DesignationBuilder designation = Designation.builder();

        designation.code( dto.getCode() );
        designation.createdAt( dto.getCreatedAt() );
        designation.createdBy( dto.getCreatedBy() );
        designation.description( dto.getDescription() );
        designation.id( dto.getId() );
        designation.isActive( dto.getIsActive() );
        designation.level( dto.getLevel() );
        designation.name( dto.getName() );
        designation.updatedAt( dto.getUpdatedAt() );
        designation.updatedBy( dto.getUpdatedBy() );

        return designation.build();
    }

    @Override
    public DesignationDto toDto(Designation entity) {
        if ( entity == null ) {
            return null;
        }

        DesignationDto.DesignationDtoBuilder designationDto = DesignationDto.builder();

        designationDto.code( entity.getCode() );
        designationDto.createdAt( entity.getCreatedAt() );
        designationDto.createdBy( entity.getCreatedBy() );
        designationDto.description( entity.getDescription() );
        designationDto.id( entity.getId() );
        designationDto.isActive( entity.getIsActive() );
        designationDto.level( entity.getLevel() );
        designationDto.name( entity.getName() );
        designationDto.updatedAt( entity.getUpdatedAt() );
        designationDto.updatedBy( entity.getUpdatedBy() );

        return designationDto.build();
    }
}
