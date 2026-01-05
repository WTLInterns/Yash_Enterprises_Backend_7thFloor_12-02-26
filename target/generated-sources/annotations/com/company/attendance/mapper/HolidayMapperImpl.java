package com.company.attendance.mapper;

import com.company.attendance.dto.HolidayDto;
import com.company.attendance.entity.Holiday;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
public class HolidayMapperImpl implements HolidayMapper {

    @Override
    public Holiday toEntity(HolidayDto dto) {
        if ( dto == null ) {
            return null;
        }

        Holiday.HolidayBuilder holiday = Holiday.builder();

        holiday.createdAt( dto.getCreatedAt() );
        holiday.createdBy( dto.getCreatedBy() );
        holiday.date( dto.getDate() );
        holiday.description( dto.getDescription() );
        holiday.id( dto.getId() );
        holiday.isActive( dto.getIsActive() );
        holiday.isOptional( dto.getIsOptional() );
        holiday.name( dto.getName() );
        holiday.type( dto.getType() );
        holiday.updatedAt( dto.getUpdatedAt() );
        holiday.updatedBy( dto.getUpdatedBy() );

        return holiday.build();
    }

    @Override
    public HolidayDto toDto(Holiday entity) {
        if ( entity == null ) {
            return null;
        }

        HolidayDto.HolidayDtoBuilder holidayDto = HolidayDto.builder();

        holidayDto.createdAt( entity.getCreatedAt() );
        holidayDto.createdBy( entity.getCreatedBy() );
        holidayDto.date( entity.getDate() );
        holidayDto.description( entity.getDescription() );
        holidayDto.id( entity.getId() );
        holidayDto.isActive( entity.getIsActive() );
        holidayDto.isOptional( entity.getIsOptional() );
        holidayDto.name( entity.getName() );
        holidayDto.type( entity.getType() );
        holidayDto.updatedAt( entity.getUpdatedAt() );
        holidayDto.updatedBy( entity.getUpdatedBy() );

        return holidayDto.build();
    }
}
