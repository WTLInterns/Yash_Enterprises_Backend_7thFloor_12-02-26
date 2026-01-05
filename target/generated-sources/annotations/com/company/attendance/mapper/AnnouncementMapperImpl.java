package com.company.attendance.mapper;

import com.company.attendance.dto.AnnouncementDto;
import com.company.attendance.entity.Announcement;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class AnnouncementMapperImpl implements AnnouncementMapper {

    @Override
    public Announcement toEntity(AnnouncementDto dto) {
        if ( dto == null ) {
            return null;
        }

        Announcement.AnnouncementBuilder announcement = Announcement.builder();

        announcement.content( dto.getContent() );
        announcement.createdAt( dto.getCreatedAt() );
        announcement.createdBy( dto.getCreatedBy() );
        announcement.id( dto.getId() );
        announcement.isActive( dto.getIsActive() );
        announcement.isPinned( dto.getIsPinned() );
        announcement.publishFrom( dto.getPublishFrom() );
        announcement.publishTo( dto.getPublishTo() );
        announcement.title( dto.getTitle() );
        announcement.type( dto.getType() );
        announcement.updatedAt( dto.getUpdatedAt() );
        announcement.updatedBy( dto.getUpdatedBy() );

        return announcement.build();
    }

    @Override
    public AnnouncementDto toDto(Announcement entity) {
        if ( entity == null ) {
            return null;
        }

        AnnouncementDto.AnnouncementDtoBuilder announcementDto = AnnouncementDto.builder();

        announcementDto.content( entity.getContent() );
        announcementDto.createdAt( entity.getCreatedAt() );
        announcementDto.createdBy( entity.getCreatedBy() );
        announcementDto.id( entity.getId() );
        announcementDto.isActive( entity.getIsActive() );
        announcementDto.isPinned( entity.getIsPinned() );
        announcementDto.publishFrom( entity.getPublishFrom() );
        announcementDto.publishTo( entity.getPublishTo() );
        announcementDto.title( entity.getTitle() );
        announcementDto.type( entity.getType() );
        announcementDto.updatedAt( entity.getUpdatedAt() );
        announcementDto.updatedBy( entity.getUpdatedBy() );

        return announcementDto.build();
    }

    @Override
    public void updateEntityFromDto(AnnouncementDto dto, Announcement entity) {
        if ( dto == null ) {
            return;
        }

        entity.setContent( dto.getContent() );
        entity.setCreatedBy( dto.getCreatedBy() );
        entity.setIsActive( dto.getIsActive() );
        entity.setIsPinned( dto.getIsPinned() );
        entity.setPublishFrom( dto.getPublishFrom() );
        entity.setPublishTo( dto.getPublishTo() );
        entity.setType( dto.getType() );
        entity.setUpdatedBy( dto.getUpdatedBy() );
        entity.setId( dto.getId() );
        entity.setTitle( dto.getTitle() );
        entity.setCreatedAt( dto.getCreatedAt() );
        entity.setUpdatedAt( dto.getUpdatedAt() );
    }
}
