package com.company.attendance.mapper;

import com.company.attendance.dto.FormDto;
import com.company.attendance.entity.Form;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class FormMapperImpl implements FormMapper {

    @Override
    public FormDto toDto(Form form) {
        if ( form == null ) {
            return null;
        }

        FormDto formDto = new FormDto();

        formDto.setCreatedBy( form.getCreatedBy() );
        formDto.setId( form.getId() );
        formDto.setIsActive( form.getIsActive() );
        formDto.setName( form.getName() );
        formDto.setSchema( form.getSchema() );

        return formDto;
    }

    @Override
    public Form toEntity(FormDto dto) {
        if ( dto == null ) {
            return null;
        }

        Form.FormBuilder form = Form.builder();

        form.createdBy( dto.getCreatedBy() );
        form.id( dto.getId() );
        form.isActive( dto.getIsActive() );
        form.name( dto.getName() );
        form.schema( dto.getSchema() );

        return form.build();
    }
}
