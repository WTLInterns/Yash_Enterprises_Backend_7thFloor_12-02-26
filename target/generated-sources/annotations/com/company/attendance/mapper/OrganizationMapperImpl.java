package com.company.attendance.mapper;

import com.company.attendance.dto.OrganizationDto;
import com.company.attendance.entity.Organization;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class OrganizationMapperImpl implements OrganizationMapper {

    @Override
    public Organization toEntity(OrganizationDto dto) {
        if ( dto == null ) {
            return null;
        }

        Organization.OrganizationBuilder organization = Organization.builder();

        organization.address( dto.getAddress() );
        organization.city( dto.getCity() );
        organization.code( dto.getCode() );
        organization.contactEmail( dto.getContactEmail() );
        organization.contactPhone( dto.getContactPhone() );
        organization.country( dto.getCountry() );
        organization.currency( dto.getCurrency() );
        organization.dateFormat( dto.getDateFormat() );
        organization.description( dto.getDescription() );
        organization.fiscalYearEnd( dto.getFiscalYearEnd() );
        organization.fiscalYearStart( dto.getFiscalYearStart() );
        organization.industry( dto.getIndustry() );
        organization.isActive( dto.getIsActive() );
        organization.logo( dto.getLogo() );
        organization.name( dto.getName() );
        organization.pincode( dto.getPincode() );
        organization.primaryColor( dto.getPrimaryColor() );
        organization.registrationNumber( dto.getRegistrationNumber() );
        organization.secondaryColor( dto.getSecondaryColor() );
        organization.state( dto.getState() );
        organization.taxId( dto.getTaxId() );
        organization.timeFormat( dto.getTimeFormat() );
        organization.timezone( dto.getTimezone() );
        organization.website( dto.getWebsite() );

        return organization.build();
    }

    @Override
    public OrganizationDto toDto(Organization entity) {
        if ( entity == null ) {
            return null;
        }

        OrganizationDto.OrganizationDtoBuilder<?, ?> organizationDto = OrganizationDto.builder();

        organizationDto.createdAt( entity.getCreatedAt() );
        organizationDto.updatedAt( entity.getUpdatedAt() );
        organizationDto.createdBy( entity.getCreatedBy() );
        organizationDto.id( entity.getId() );
        organizationDto.updatedBy( entity.getUpdatedBy() );
        organizationDto.address( entity.getAddress() );
        organizationDto.city( entity.getCity() );
        organizationDto.code( entity.getCode() );
        organizationDto.contactEmail( entity.getContactEmail() );
        organizationDto.contactPhone( entity.getContactPhone() );
        organizationDto.country( entity.getCountry() );
        organizationDto.currency( entity.getCurrency() );
        organizationDto.dateFormat( entity.getDateFormat() );
        organizationDto.description( entity.getDescription() );
        organizationDto.fiscalYearEnd( entity.getFiscalYearEnd() );
        organizationDto.fiscalYearStart( entity.getFiscalYearStart() );
        organizationDto.industry( entity.getIndustry() );
        organizationDto.isActive( entity.getIsActive() );
        organizationDto.logo( entity.getLogo() );
        organizationDto.name( entity.getName() );
        organizationDto.pincode( entity.getPincode() );
        organizationDto.primaryColor( entity.getPrimaryColor() );
        organizationDto.registrationNumber( entity.getRegistrationNumber() );
        organizationDto.secondaryColor( entity.getSecondaryColor() );
        organizationDto.state( entity.getState() );
        organizationDto.taxId( entity.getTaxId() );
        organizationDto.timeFormat( entity.getTimeFormat() );
        organizationDto.timezone( entity.getTimezone() );
        organizationDto.website( entity.getWebsite() );

        return organizationDto.build();
    }

    @Override
    public List<OrganizationDto> toDtoList(List<Organization> entities) {
        if ( entities == null ) {
            return null;
        }

        List<OrganizationDto> list = new ArrayList<OrganizationDto>( entities.size() );
        for ( Organization organization : entities ) {
            list.add( toDto( organization ) );
        }

        return list;
    }

    @Override
    public void updateOrganizationFromDto(OrganizationDto dto, Organization entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getAddress() != null ) {
            entity.setAddress( dto.getAddress() );
        }
        if ( dto.getCity() != null ) {
            entity.setCity( dto.getCity() );
        }
        if ( dto.getCode() != null ) {
            entity.setCode( dto.getCode() );
        }
        if ( dto.getContactEmail() != null ) {
            entity.setContactEmail( dto.getContactEmail() );
        }
        if ( dto.getContactPhone() != null ) {
            entity.setContactPhone( dto.getContactPhone() );
        }
        if ( dto.getCountry() != null ) {
            entity.setCountry( dto.getCountry() );
        }
        if ( dto.getCurrency() != null ) {
            entity.setCurrency( dto.getCurrency() );
        }
        if ( dto.getDateFormat() != null ) {
            entity.setDateFormat( dto.getDateFormat() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getFiscalYearEnd() != null ) {
            entity.setFiscalYearEnd( dto.getFiscalYearEnd() );
        }
        if ( dto.getFiscalYearStart() != null ) {
            entity.setFiscalYearStart( dto.getFiscalYearStart() );
        }
        if ( dto.getIndustry() != null ) {
            entity.setIndustry( dto.getIndustry() );
        }
        if ( dto.getIsActive() != null ) {
            entity.setIsActive( dto.getIsActive() );
        }
        if ( dto.getLogo() != null ) {
            entity.setLogo( dto.getLogo() );
        }
        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
        if ( dto.getPincode() != null ) {
            entity.setPincode( dto.getPincode() );
        }
        if ( dto.getPrimaryColor() != null ) {
            entity.setPrimaryColor( dto.getPrimaryColor() );
        }
        if ( dto.getRegistrationNumber() != null ) {
            entity.setRegistrationNumber( dto.getRegistrationNumber() );
        }
        if ( dto.getSecondaryColor() != null ) {
            entity.setSecondaryColor( dto.getSecondaryColor() );
        }
        if ( dto.getState() != null ) {
            entity.setState( dto.getState() );
        }
        if ( dto.getTaxId() != null ) {
            entity.setTaxId( dto.getTaxId() );
        }
        if ( dto.getTimeFormat() != null ) {
            entity.setTimeFormat( dto.getTimeFormat() );
        }
        if ( dto.getTimezone() != null ) {
            entity.setTimezone( dto.getTimezone() );
        }
        if ( dto.getWebsite() != null ) {
            entity.setWebsite( dto.getWebsite() );
        }
    }
}
