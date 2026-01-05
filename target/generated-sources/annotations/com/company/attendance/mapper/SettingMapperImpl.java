package com.company.attendance.mapper;

import com.company.attendance.dto.SettingDto;
import com.company.attendance.entity.Setting;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class SettingMapperImpl implements SettingMapper {

    @Override
    public SettingDto toDto(Setting o) {
        if ( o == null ) {
            return null;
        }

        SettingDto settingDto = new SettingDto();

        settingDto.setId( o.getId() );
        settingDto.setKey( o.getKey() );
        settingDto.setValue( o.getValue() );

        return settingDto;
    }

    @Override
    public Setting toEntity(SettingDto dto) {
        if ( dto == null ) {
            return null;
        }

        Setting.SettingBuilder setting = Setting.builder();

        setting.id( dto.getId() );
        setting.key( dto.getKey() );
        setting.value( dto.getValue() );

        return setting.build();
    }
}
