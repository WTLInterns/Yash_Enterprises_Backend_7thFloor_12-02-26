package com.company.attendance.mapper;

import com.company.attendance.dto.ClientDto;
import com.company.attendance.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);
    
    // ClientDto -> Client
    @Mapping(target = "id", ignore = true) // if id auto-generated, else remove
    Client toEntity(ClientDto dto);

    // Client -> ClientDto
    ClientDto toDto(Client client);

    List<ClientDto> toDtoList(List<Client> clients);

    List<Client> toEntityList(List<ClientDto> dtos);

    // update method
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ClientDto dto, @MappingTarget Client client);
}
