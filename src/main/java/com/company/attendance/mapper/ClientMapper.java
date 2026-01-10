package com.company.attendance.mapper;

import com.company.attendance.dto.ClientDto;
import com.company.attendance.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);
    
    default Long uuidToLong(UUID uuid) {
        return uuid != null ? uuid.getMostSignificantBits() : null;
    }
    
    default UUID longToUuid(Long value) {
        if (value == null) return null;
        return new UUID(value, 0L);
    }
    
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ClientDto toDto(Client client);
    
    @Mapping(target = "cases", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Client toEntity(ClientDto clientDto);
    
    @Mapping(target = "cases", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromDto(ClientDto clientDto, @MappingTarget Client client);
    
    List<ClientDto> toDtoList(List<Client> clients);
    List<Client> toEntityList(List<ClientDto> clientDtos);
}
