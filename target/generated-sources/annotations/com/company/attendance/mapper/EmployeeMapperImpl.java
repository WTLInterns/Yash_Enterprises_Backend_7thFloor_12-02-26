package com.company.attendance.mapper;

import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.entity.Department;
import com.company.attendance.entity.Designation;
import com.company.attendance.entity.Employee;
import com.company.attendance.entity.Organization;
import com.company.attendance.entity.Role;
import com.company.attendance.entity.Shift;
import com.company.attendance.entity.Team;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public Employee toEntity(EmployeeDto dto) {
        if ( dto == null ) {
            return null;
        }

        Employee.EmployeeBuilder employee = Employee.builder();

        employee.attendanceAllowed( dto.getAttendanceAllowed() );
        employee.customDesignation( dto.getCustomDesignation() );
        employee.dateOfBirth( dto.getDateOfBirth() );
        employee.email( dto.getEmail() );
        employee.employeeCode( dto.getEmployeeCode() );
        employee.employeeId( dto.getEmployeeId() );
        employee.firstName( dto.getFirstName() );
        employee.gender( dto.getGender() );
        employee.hiredAt( dto.getHiredAt() );
        employee.id( dto.getId() );
        employee.lastName( dto.getLastName() );
        employee.locationLat( dto.getLocationLat() );
        employee.locationLng( dto.getLocationLng() );
        employee.phone( dto.getPhone() );
        employee.profileImageUrl( dto.getProfileImageUrl() );
        if ( dto.getStatus() != null ) {
            employee.status( Enum.valueOf( Employee.Status.class, dto.getStatus() ) );
        }
        employee.subadminId( dto.getSubadminId() );
        employee.terminationDate( dto.getTerminationDate() );
        employee.userId( dto.getUserId() );

        return employee.build();
    }

    @Override
    public EmployeeDto toDto(Employee entity) {
        if ( entity == null ) {
            return null;
        }

        EmployeeDto.EmployeeDtoBuilder employeeDto = EmployeeDto.builder();

        employeeDto.roleId( entityRoleId( entity ) );
        employeeDto.roleName( entityRoleName( entity ) );
        employeeDto.teamId( entityTeamId( entity ) );
        employeeDto.teamName( entityTeamName( entity ) );
        employeeDto.designationId( entityDesignationId( entity ) );
        employeeDto.designationName( entityDesignationName( entity ) );
        employeeDto.reportingManagerId( entityReportingManagerId( entity ) );
        employeeDto.organizationId( entityOrganizationId( entity ) );
        employeeDto.organizationName( entityOrganizationName( entity ) );
        employeeDto.departmentId( entityDepartmentId( entity ) );
        employeeDto.departmentName( entityDepartmentName( entity ) );
        employeeDto.shiftId( entityShiftId( entity ) );
        employeeDto.shiftName( entityShiftName( entity ) );
        employeeDto.attendanceAllowed( entity.getAttendanceAllowed() );
        employeeDto.customDesignation( entity.getCustomDesignation() );
        employeeDto.dateOfBirth( entity.getDateOfBirth() );
        employeeDto.email( entity.getEmail() );
        employeeDto.employeeCode( entity.getEmployeeCode() );
        employeeDto.employeeId( entity.getEmployeeId() );
        employeeDto.firstName( entity.getFirstName() );
        employeeDto.gender( entity.getGender() );
        employeeDto.hiredAt( entity.getHiredAt() );
        employeeDto.id( entity.getId() );
        employeeDto.lastName( entity.getLastName() );
        employeeDto.locationLat( entity.getLocationLat() );
        employeeDto.locationLng( entity.getLocationLng() );
        employeeDto.phone( entity.getPhone() );
        employeeDto.profileImageUrl( entity.getProfileImageUrl() );
        if ( entity.getStatus() != null ) {
            employeeDto.status( entity.getStatus().name() );
        }
        employeeDto.subadminId( entity.getSubadminId() );
        employeeDto.terminationDate( entity.getTerminationDate() );
        employeeDto.userId( entity.getUserId() );

        employeeDto.reportingManagerName( entity.getReportingManager() != null ? entity.getReportingManager().getFirstName() + " " + entity.getReportingManager().getLastName() : null );

        return employeeDto.build();
    }

    @Override
    public void updateEntityFromDto(EmployeeDto dto, Employee entity) {
        if ( dto == null ) {
            return;
        }

        entity.setAttendanceAllowed( dto.getAttendanceAllowed() );
        entity.setCreatedBy( dto.getCreatedBy() );
        entity.setCustomDesignation( dto.getCustomDesignation() );
        entity.setDateOfBirth( dto.getDateOfBirth() );
        entity.setEmployeeCode( dto.getEmployeeCode() );
        entity.setGender( dto.getGender() );
        entity.setHiredAt( dto.getHiredAt() );
        entity.setLocationLat( dto.getLocationLat() );
        entity.setLocationLng( dto.getLocationLng() );
        entity.setProfileImageUrl( dto.getProfileImageUrl() );
        if ( dto.getStatus() != null ) {
            entity.setStatus( Enum.valueOf( Employee.Status.class, dto.getStatus() ) );
        }
        else {
            entity.setStatus( null );
        }
        entity.setSubadminId( dto.getSubadminId() );
        entity.setTerminationDate( dto.getTerminationDate() );
        entity.setUpdatedBy( dto.getUpdatedBy() );
        entity.setFirstName( dto.getFirstName() );
        entity.setLastName( dto.getLastName() );
        entity.setEmail( dto.getEmail() );
        entity.setPhone( dto.getPhone() );
    }

    private Long entityRoleId(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Role role = employee.getRole();
        if ( role == null ) {
            return null;
        }
        Long id = role.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityRoleName(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Role role = employee.getRole();
        if ( role == null ) {
            return null;
        }
        String name = role.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityTeamId(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Team team = employee.getTeam();
        if ( team == null ) {
            return null;
        }
        Long id = team.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityTeamName(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Team team = employee.getTeam();
        if ( team == null ) {
            return null;
        }
        String name = team.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityDesignationId(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Designation designation = employee.getDesignation();
        if ( designation == null ) {
            return null;
        }
        Long id = designation.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityDesignationName(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Designation designation = employee.getDesignation();
        if ( designation == null ) {
            return null;
        }
        String name = designation.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityReportingManagerId(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Employee reportingManager = employee.getReportingManager();
        if ( reportingManager == null ) {
            return null;
        }
        Long id = reportingManager.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityOrganizationId(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Organization organization = employee.getOrganization();
        if ( organization == null ) {
            return null;
        }
        Long id = organization.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityOrganizationName(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Organization organization = employee.getOrganization();
        if ( organization == null ) {
            return null;
        }
        String name = organization.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityDepartmentId(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Department department = employee.getDepartment();
        if ( department == null ) {
            return null;
        }
        Long id = department.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityDepartmentName(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Department department = employee.getDepartment();
        if ( department == null ) {
            return null;
        }
        String name = department.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityShiftId(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Shift shift = employee.getShift();
        if ( shift == null ) {
            return null;
        }
        Long id = shift.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityShiftName(Employee employee) {
        if ( employee == null ) {
            return null;
        }
        Shift shift = employee.getShift();
        if ( shift == null ) {
            return null;
        }
        String name = shift.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
