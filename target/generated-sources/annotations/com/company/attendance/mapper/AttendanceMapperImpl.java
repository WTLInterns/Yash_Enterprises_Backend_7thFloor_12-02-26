package com.company.attendance.mapper;

import com.company.attendance.dto.AttendanceDto;
import com.company.attendance.entity.Attendance;
import com.company.attendance.entity.Employee;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class AttendanceMapperImpl implements AttendanceMapper {

    @Override
    public AttendanceDto toDto(Attendance attendance) {
        if ( attendance == null ) {
            return null;
        }

        AttendanceDto attendanceDto = new AttendanceDto();

        attendanceDto.setEmployeeId( attendanceEmployeeId( attendance ) );
        if ( attendance.getStatus() != null ) {
            attendanceDto.setStatus( attendance.getStatus().name() );
        }
        attendanceDto.setDate( attendance.getDate() );
        attendanceDto.setDeviceInfo( attendance.getDeviceInfo() );
        attendanceDto.setId( attendance.getId() );
        attendanceDto.setInLocationLat( attendance.getInLocationLat() );
        attendanceDto.setInLocationLng( attendance.getInLocationLng() );
        attendanceDto.setIsLunchIn( attendance.getIsLunchIn() );
        attendanceDto.setIsLunchOut( attendance.getIsLunchOut() );
        attendanceDto.setNote( attendance.getNote() );
        attendanceDto.setOutLocationLat( attendance.getOutLocationLat() );
        attendanceDto.setOutLocationLng( attendance.getOutLocationLng() );
        attendanceDto.setPunchInTime( attendance.getPunchInTime() );
        attendanceDto.setPunchOutTime( attendance.getPunchOutTime() );

        return attendanceDto;
    }

    @Override
    public Attendance toEntity(AttendanceDto dto) {
        if ( dto == null ) {
            return null;
        }

        Attendance.AttendanceBuilder attendance = Attendance.builder();

        attendance.date( dto.getDate() );
        attendance.deviceInfo( dto.getDeviceInfo() );
        attendance.inLocationLat( dto.getInLocationLat() );
        attendance.inLocationLng( dto.getInLocationLng() );
        attendance.isLunchIn( dto.getIsLunchIn() );
        attendance.isLunchOut( dto.getIsLunchOut() );
        attendance.note( dto.getNote() );
        attendance.outLocationLat( dto.getOutLocationLat() );
        attendance.outLocationLng( dto.getOutLocationLng() );
        attendance.punchInTime( dto.getPunchInTime() );
        attendance.punchOutTime( dto.getPunchOutTime() );
        if ( dto.getStatus() != null ) {
            attendance.status( Enum.valueOf( Attendance.Status.class, dto.getStatus() ) );
        }

        return attendance.build();
    }

    @Override
    public void updateEntityFromDto(AttendanceDto dto, Attendance attendance) {
        if ( dto == null ) {
            return;
        }

        attendance.setDate( dto.getDate() );
        attendance.setDeviceInfo( dto.getDeviceInfo() );
        attendance.setInLocationLat( dto.getInLocationLat() );
        attendance.setInLocationLng( dto.getInLocationLng() );
        attendance.setIsLunchIn( dto.getIsLunchIn() );
        attendance.setIsLunchOut( dto.getIsLunchOut() );
        attendance.setNote( dto.getNote() );
        attendance.setOutLocationLat( dto.getOutLocationLat() );
        attendance.setOutLocationLng( dto.getOutLocationLng() );
        attendance.setPunchInTime( dto.getPunchInTime() );
        attendance.setPunchOutTime( dto.getPunchOutTime() );
        if ( dto.getStatus() != null ) {
            attendance.setStatus( Enum.valueOf( Attendance.Status.class, dto.getStatus() ) );
        }
        else {
            attendance.setStatus( null );
        }
    }

    private Long attendanceEmployeeId(Attendance attendance) {
        if ( attendance == null ) {
            return null;
        }
        Employee employee = attendance.getEmployee();
        if ( employee == null ) {
            return null;
        }
        Long id = employee.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
