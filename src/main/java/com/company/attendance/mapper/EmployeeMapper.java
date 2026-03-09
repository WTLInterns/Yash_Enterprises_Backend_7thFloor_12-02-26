package com.company.attendance.mapper;

import com.company.attendance.dto.EmployeeDto;
import com.company.attendance.entity.Employee;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "reportingManager", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Employee toEntity(EmployeeDto dto);
    
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "designationId", source = "designation.id")
    @Mapping(target = "designationName", source = "designation.name")
    @Mapping(target = "reportingManagerId", source = "reportingManager.id")
    @Mapping(target = "reportingManagerName", expression = "java(entity.getReportingManager() != null ? entity.getReportingManager().getFirstName() + \" \" + entity.getReportingManager().getLastName() : null)")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", expression = "java(entity.getDepartment() != null ? entity.getDepartment().getName() : entity.getDepartmentName())")
    @Mapping(target = "shiftId", source = "shift.id")
    @Mapping(target = "shiftName", source = "shift.name")
    // ✅ NEW: Map TL info for EMPLOYEE role
    @Mapping(target = "tlId", source = "tl.id")
    @Mapping(target = "tlFirstName", source = "tl.firstName")
    @Mapping(target = "tlLastName", source = "tl.lastName")
    @Mapping(target = "tlFullName", expression = "java(entity.getTl() != null ? entity.getTl().getFullName() : null)")
    @Mapping(target = "tlDepartmentName", expression = "java(entity.getTl() != null ? (entity.getTl().getDepartmentName() != null ? entity.getTl().getDepartmentName() : (entity.getTl().getDepartment() != null ? entity.getTl().getDepartment().getName() : null)) : null)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    EmployeeDto toDto(Employee entity);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "reportingManager", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(EmployeeDto dto, @MappingTarget Employee entity);
}
