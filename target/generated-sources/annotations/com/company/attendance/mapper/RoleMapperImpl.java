package com.company.attendance.mapper;

import com.company.attendance.dto.RoleDto;
import com.company.attendance.entity.Role;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public Role toEntity(RoleDto dto) {
        if ( dto == null ) {
            return null;
        }

        Role.RoleBuilder role = Role.builder();

        role.description( dto.getDescription() );
        role.displayName( dto.getDisplayName() );
        role.id( dto.getId() );
        role.isActive( dto.getIsActive() );
        role.isDefault( dto.getIsDefault() );
        role.name( dto.getName() );

        return role.build();
    }

    @Override
    public RoleDto toDto(Role entity) {
        if ( entity == null ) {
            return null;
        }

        RoleDto.RoleDtoBuilder roleDto = RoleDto.builder();

        roleDto.description( entity.getDescription() );
        roleDto.displayName( entity.getDisplayName() );
        roleDto.id( entity.getId() );
        roleDto.isActive( entity.getIsActive() );
        roleDto.isDefault( entity.getIsDefault() );
        roleDto.name( entity.getName() );

        return roleDto.build();
    }
}
