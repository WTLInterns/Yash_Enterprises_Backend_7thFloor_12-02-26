package com.company.attendance.mapper;

import com.company.attendance.dto.PolicyDto;
import com.company.attendance.entity.Policy;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
public class PolicyMapperImpl implements PolicyMapper {

    @Override
    public Policy toEntity(PolicyDto dto) {
        if ( dto == null ) {
            return null;
        }

        Policy.PolicyBuilder policy = Policy.builder();

        policy.code( dto.getCode() );
        policy.content( dto.getContent() );
        policy.createdAt( dto.getCreatedAt() );
        policy.createdBy( dto.getCreatedBy() );
        policy.description( dto.getDescription() );
        policy.effectiveFrom( dto.getEffectiveFrom() );
        policy.effectiveTo( dto.getEffectiveTo() );
        policy.id( dto.getId() );
        policy.isActive( dto.getIsActive() );
        policy.isMandatory( dto.getIsMandatory() );
        policy.name( dto.getName() );
        policy.type( dto.getType() );
        policy.updatedAt( dto.getUpdatedAt() );
        policy.updatedBy( dto.getUpdatedBy() );

        return policy.build();
    }

    @Override
    public PolicyDto toDto(Policy entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyDto.PolicyDtoBuilder policyDto = PolicyDto.builder();

        policyDto.code( entity.getCode() );
        policyDto.content( entity.getContent() );
        policyDto.createdAt( entity.getCreatedAt() );
        policyDto.createdBy( entity.getCreatedBy() );
        policyDto.description( entity.getDescription() );
        policyDto.effectiveFrom( entity.getEffectiveFrom() );
        policyDto.effectiveTo( entity.getEffectiveTo() );
        policyDto.id( entity.getId() );
        policyDto.isActive( entity.getIsActive() );
        policyDto.isMandatory( entity.getIsMandatory() );
        policyDto.name( entity.getName() );
        policyDto.type( entity.getType() );
        policyDto.updatedAt( entity.getUpdatedAt() );
        policyDto.updatedBy( entity.getUpdatedBy() );

        return policyDto.build();
    }
}
