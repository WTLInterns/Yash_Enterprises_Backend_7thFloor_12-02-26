package com.company.attendance.mapper;

import com.company.attendance.dto.DepartmentDto;
import com.company.attendance.entity.Department;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
public class DepartmentMapperImpl implements DepartmentMapper {

    @Override
    public Department toEntity(DepartmentDto dto) {
        if ( dto == null ) {
            return null;
        }

        Department.DepartmentBuilder department = Department.builder();

        department.code( dto.getCode() );
        department.createdAt( dto.getCreatedAt() );
        department.createdBy( dto.getCreatedBy() );
        department.description( dto.getDescription() );
        department.headOfDepartmentId( dto.getHeadOfDepartmentId() );
        department.id( dto.getId() );
        department.isActive( dto.getIsActive() );
        department.name( dto.getName() );
        department.updatedAt( dto.getUpdatedAt() );
        department.updatedBy( dto.getUpdatedBy() );

        return department.build();
    }

    @Override
    public DepartmentDto toDto(Department entity) {
        if ( entity == null ) {
            return null;
        }

        DepartmentDto.DepartmentDtoBuilder departmentDto = DepartmentDto.builder();

        departmentDto.code( entity.getCode() );
        departmentDto.createdAt( entity.getCreatedAt() );
        departmentDto.createdBy( entity.getCreatedBy() );
        departmentDto.description( entity.getDescription() );
        departmentDto.headOfDepartmentId( entity.getHeadOfDepartmentId() );
        departmentDto.id( entity.getId() );
        departmentDto.isActive( entity.getIsActive() );
        departmentDto.name( entity.getName() );
        departmentDto.updatedAt( entity.getUpdatedAt() );
        departmentDto.updatedBy( entity.getUpdatedBy() );

        return departmentDto.build();
    }
}
