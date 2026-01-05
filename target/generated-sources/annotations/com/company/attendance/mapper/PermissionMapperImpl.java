package com.company.attendance.mapper;

import com.company.attendance.dto.PermissionDto;
import com.company.attendance.entity.Permission;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public Permission toEntity(PermissionDto dto) {
        if ( dto == null ) {
            return null;
        }

        Permission.PermissionBuilder permission = Permission.builder();

        permission.action( dto.getAction() );
        permission.description( dto.getDescription() );
        permission.displayName( dto.getDisplayName() );
        permission.id( dto.getId() );
        permission.isActive( dto.getIsActive() );
        permission.module( dto.getModule() );
        permission.name( dto.getName() );
        permission.resource( dto.getResource() );

        return permission.build();
    }

    @Override
    public PermissionDto toDto(Permission entity) {
        if ( entity == null ) {
            return null;
        }

        PermissionDto.PermissionDtoBuilder permissionDto = PermissionDto.builder();

        permissionDto.action( entity.getAction() );
        permissionDto.description( entity.getDescription() );
        permissionDto.displayName( entity.getDisplayName() );
        permissionDto.id( entity.getId() );
        permissionDto.isActive( entity.getIsActive() );
        permissionDto.module( entity.getModule() );
        permissionDto.name( entity.getName() );
        permissionDto.resource( entity.getResource() );

        return permissionDto.build();
    }
}
