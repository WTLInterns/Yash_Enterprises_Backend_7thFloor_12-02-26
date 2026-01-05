package com.company.attendance.mapper;

import com.company.attendance.dto.ShiftDto;
import com.company.attendance.entity.Shift;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
public class ShiftMapperImpl implements ShiftMapper {

    @Override
    public Shift toEntity(ShiftDto dto) {
        if ( dto == null ) {
            return null;
        }

        Shift.ShiftBuilder shift = Shift.builder();

        shift.breakEndTime( dto.getBreakEndTime() );
        shift.breakStartTime( dto.getBreakStartTime() );
        shift.code( dto.getCode() );
        shift.createdAt( dto.getCreatedAt() );
        shift.createdBy( dto.getCreatedBy() );
        shift.description( dto.getDescription() );
        shift.endTime( dto.getEndTime() );
        shift.gracePeriodInMinutes( dto.getGracePeriodInMinutes() );
        shift.id( dto.getId() );
        shift.isActive( dto.getIsActive() );
        shift.isOverTimeAllowed( dto.getIsOverTimeAllowed() );
        shift.name( dto.getName() );
        shift.overtimeThresholdInMinutes( dto.getOvertimeThresholdInMinutes() );
        shift.startTime( dto.getStartTime() );
        shift.updatedAt( dto.getUpdatedAt() );
        shift.updatedBy( dto.getUpdatedBy() );

        return shift.build();
    }

    @Override
    public ShiftDto toDto(Shift entity) {
        if ( entity == null ) {
            return null;
        }

        ShiftDto.ShiftDtoBuilder shiftDto = ShiftDto.builder();

        shiftDto.breakEndTime( entity.getBreakEndTime() );
        shiftDto.breakStartTime( entity.getBreakStartTime() );
        shiftDto.code( entity.getCode() );
        shiftDto.createdAt( entity.getCreatedAt() );
        shiftDto.createdBy( entity.getCreatedBy() );
        shiftDto.description( entity.getDescription() );
        shiftDto.endTime( entity.getEndTime() );
        shiftDto.gracePeriodInMinutes( entity.getGracePeriodInMinutes() );
        shiftDto.id( entity.getId() );
        shiftDto.isActive( entity.getIsActive() );
        shiftDto.isOverTimeAllowed( entity.getIsOverTimeAllowed() );
        shiftDto.name( entity.getName() );
        shiftDto.overtimeThresholdInMinutes( entity.getOvertimeThresholdInMinutes() );
        shiftDto.startTime( entity.getStartTime() );
        shiftDto.updatedAt( entity.getUpdatedAt() );
        shiftDto.updatedBy( entity.getUpdatedBy() );

        return shiftDto.build();
    }

    @Override
    public List<ShiftDto> toDtoList(List<Shift> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ShiftDto> list = new ArrayList<ShiftDto>( entities.size() );
        for ( Shift shift : entities ) {
            list.add( toDto( shift ) );
        }

        return list;
    }
}
