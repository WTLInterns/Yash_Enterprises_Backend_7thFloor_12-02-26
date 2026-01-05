package com.company.attendance.mapper;

import com.company.attendance.dto.SettingsDto;
import com.company.attendance.entity.Settings;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class SettingsMapperImpl implements SettingsMapper {

    @Override
    public SettingsDto toDto(Settings entity) {
        if ( entity == null ) {
            return null;
        }

        SettingsDto settingsDto = new SettingsDto();

        settingsDto.setCategory( entity.getCategory() );
        settingsDto.setDataType( entity.getDataType() );
        settingsDto.setDescription( entity.getDescription() );
        settingsDto.setId( entity.getId() );
        settingsDto.setIsEditable( entity.getIsEditable() );
        settingsDto.setValue( entity.getValue() );

        return settingsDto;
    }

    @Override
    public Settings toEntity(SettingsDto dto) {
        if ( dto == null ) {
            return null;
        }

        Settings.SettingsBuilder settings = Settings.builder();

        settings.category( dto.getCategory() );
        settings.dataType( dto.getDataType() );
        settings.description( dto.getDescription() );
        settings.isEditable( dto.getIsEditable() );
        settings.value( dto.getValue() );

        return settings.build();
    }

    @Override
    public void updateEntityFromDto(SettingsDto dto, Settings entity) {
        if ( dto == null ) {
            return;
        }

        entity.setCategory( dto.getCategory() );
        entity.setDataType( dto.getDataType() );
        entity.setDescription( dto.getDescription() );
        entity.setIsEditable( dto.getIsEditable() );
        entity.setValue( dto.getValue() );
    }
}
