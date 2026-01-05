package com.company.attendance.mapper;

import com.company.attendance.dto.ClientDto;
import com.company.attendance.entity.Client;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ClientMapperImpl implements ClientMapper {

    @Override
    public ClientDto toDto(Client client) {
        if ( client == null ) {
            return null;
        }

        ClientDto clientDto = new ClientDto();

        clientDto.setAddress( client.getAddress() );
        clientDto.setContactPhone( client.getContactPhone() );
        clientDto.setCreatedAt( client.getCreatedAt() );
        clientDto.setId( client.getId() );
        clientDto.setIsActive( client.getIsActive() );
        clientDto.setNotes( client.getNotes() );
        clientDto.setUpdatedAt( client.getUpdatedAt() );
        clientDto.setName( client.getName() );
        clientDto.setEmail( client.getEmail() );

        return clientDto;
    }

    @Override
    public Client toEntity(ClientDto clientDto) {
        if ( clientDto == null ) {
            return null;
        }

        Client.ClientBuilder client = Client.builder();

        client.address( clientDto.getAddress() );
        client.contactPhone( clientDto.getContactPhone() );
        client.createdAt( clientDto.getCreatedAt() );
        client.email( clientDto.getEmail() );
        client.id( clientDto.getId() );
        client.isActive( clientDto.getIsActive() );
        client.name( clientDto.getName() );
        client.notes( clientDto.getNotes() );
        client.updatedAt( clientDto.getUpdatedAt() );

        return client.build();
    }

    @Override
    public void updateEntityFromDto(ClientDto clientDto, Client client) {
        if ( clientDto == null ) {
            return;
        }

        client.setAddress( clientDto.getAddress() );
        client.setContactPhone( clientDto.getContactPhone() );
        client.setCreatedAt( clientDto.getCreatedAt() );
        client.setEmail( clientDto.getEmail() );
        client.setId( clientDto.getId() );
        client.setIsActive( clientDto.getIsActive() );
        client.setName( clientDto.getName() );
        client.setNotes( clientDto.getNotes() );
        client.setUpdatedAt( clientDto.getUpdatedAt() );
    }

    @Override
    public List<ClientDto> toDtoList(List<Client> clients) {
        if ( clients == null ) {
            return null;
        }

        List<ClientDto> list = new ArrayList<ClientDto>( clients.size() );
        for ( Client client : clients ) {
            list.add( toDto( client ) );
        }

        return list;
    }

    @Override
    public List<Client> toEntityList(List<ClientDto> clientDtos) {
        if ( clientDtos == null ) {
            return null;
        }

        List<Client> list = new ArrayList<Client>( clientDtos.size() );
        for ( ClientDto clientDto : clientDtos ) {
            list.add( toEntity( clientDto ) );
        }

        return list;
    }
}
