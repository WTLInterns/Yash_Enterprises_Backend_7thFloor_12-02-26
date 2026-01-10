package com.company.attendance.service;

import com.company.attendance.dto.ClientDto;
import com.company.attendance.entity.Client;
import com.company.attendance.mapper.ClientMapper;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.crm.mapper.CrmMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {
    
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final CrmMapper crmMapper;

    // CRM Entity methods
    public List<Client> getAllClientEntities() {
        log.info("Fetching all client entities");
        return clientRepository.findAll();
    }

    public List<Client> getActiveClientEntities() {
        log.info("Fetching active client entities");
        return clientRepository.findByIsActive(true);
    }

    public Client getClientEntityById(UUID id) {
        log.info("Fetching client entity with ID: {}", id);
        return clientRepository.findById(id).orElse(null);
    }

    public Client createClientEntity(Client client) {
        log.info("Creating new client entity: {}", client.getName());
        
        // Check if email already exists
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + client.getEmail());
        }
        
        Client savedClient = clientRepository.save(client);
        log.info("Client entity created successfully with ID: {}", savedClient.getId());
        return savedClient;
    }

    @Transactional
    public Client updateClientEntity(UUID id, Client client) {
        log.info("Updating client entity with ID: {}", id);
        
        // Find existing client
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        // Check if email is being changed and if new email already exists
        if (client.getEmail() != null && !client.getEmail().equals(existingClient.getEmail())) {
            clientRepository.findByEmail(client.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new RuntimeException("Email already exists: " + client.getEmail());
                        }
                    });
        }
        
        // Update fields
        if (client.getName() != null) {
            existingClient.setName(client.getName());
        }
        if (client.getEmail() != null) {
            existingClient.setEmail(client.getEmail());
        }
        if (client.getContactPhone() != null) {
            existingClient.setContactPhone(client.getContactPhone());
        }
        if (client.getAddress() != null) {
            existingClient.setAddress(client.getAddress());
        }
        if (client.getNotes() != null) {
            existingClient.setNotes(client.getNotes());
        }
        if (client.isActive() != existingClient.isActive()) {
            existingClient.setIsActive(client.isActive());
        }
        if (client.getCustomFields() != null) {
            existingClient.setCustomFields(client.getCustomFields());
        }
        
        // Explicitly set the ID to ensure it's not lost
        existingClient.setId(id);
        
        // Save the updated client
        Client updatedClient = clientRepository.save(existingClient);
        log.info("Client entity updated successfully with ID: {}", updatedClient.getId());
        
        return updatedClient;
    }

    public void deleteClientEntity(UUID id) {
        log.info("Deleting client entity with ID: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        // Soft delete by setting isActive to false
        client.setIsActive(false);
        clientRepository.save(client);
        
        log.info("Client entity deleted successfully with ID: {}", id);
    }

    // Legacy DTO methods (keep for compatibility)
    public ClientDto createClient(ClientDto clientDto) {
        log.info("Creating new client: {}", clientDto.getName());
        
        // Check if email already exists
        if (clientDto.getEmail() != null && clientRepository.findByEmail(clientDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + clientDto.getEmail());
        }
        
        Client client = clientMapper.toEntity(clientDto);
        Client savedClient = clientRepository.save(client);
        
        log.info("Client created successfully with ID: {}", savedClient.getId());
        return clientMapper.toDto(savedClient);
    }

    @Transactional
    public ClientDto updateClient(Long id, ClientDto clientDto) {
        log.info("Updating client with ID: {}", id);
        
        // Find existing client
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        // Check if email is being changed and if new email already exists
        if (clientDto.getEmail() != null && !clientDto.getEmail().equals(existingClient.getEmail())) {
            clientRepository.findByEmail(clientDto.getEmail())
                    .ifPresent(client -> {
                        if (!client.getId().equals(id)) {
                            throw new RuntimeException("Email already exists: " + clientDto.getEmail());
                        }
                    });
        }
        
        // Update only the fields that should be updated
        if (clientDto.getName() != null) {
            existingClient.setName(clientDto.getName());
        }
        if (clientDto.getEmail() != null) {
            existingClient.setEmail(clientDto.getEmail());
        }
        if (clientDto.getContactPhone() != null) {
            existingClient.setContactPhone(clientDto.getContactPhone());
        }
        if (clientDto.getAddress() != null) {
            existingClient.setAddress(clientDto.getAddress());
        }
        if (clientDto.getNotes() != null) {
            existingClient.setNotes(clientDto.getNotes());
        }
        if (clientDto.getIsActive() != null) {
            existingClient.setIsActive(clientDto.getIsActive());
        }
        
        // Explicitly set the ID to ensure it's not lost
        existingClient.setId(id);
        
        // Save the updated client
        Client updatedClient = clientRepository.save(existingClient);
        log.info("Client updated successfully with ID: {}", updatedClient.getId());
        
        return clientMapper.toDto(updatedClient);
    }

    public ClientDto getClientById(Long id) {
        log.info("Fetching client with ID: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        return clientMapper.toDto(client);
    }

    public List<ClientDto> getAllClients() {
        log.info("Fetching all clients");
        
        List<Client> clients = clientRepository.findAll();
        return clientMapper.toDtoList(clients);
    }

    public List<ClientDto> getActiveClients() {
        log.info("Fetching active clients");
        List<Client> clients = clientRepository.findByIsActive(true);
        return clientMapper.toDtoList(clients);
    }

    public List<ClientDto> searchClients(String search) {
        log.info("Searching clients with term: {}", search);
        
        // Simple implementation using findAll and filtering
        List<Client> allClients = clientRepository.findAll();
        return allClients.stream()
                .filter(client -> client.getName().toLowerCase().contains(search.toLowerCase()) ||
                               (client.getEmail() != null && client.getEmail().toLowerCase().contains(search.toLowerCase())))
                .map(clientMapper::toDto)
                .toList();
    }

    public void deleteClient(Long id) {
        log.info("Deleting client with ID: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        // Soft delete by setting isActive to false
        client.setIsActive(false);
        clientRepository.save(client);
        
        log.info("Client deleted successfully with ID: {}", id);
    }

    public long getActiveClientsCount() {
        return clientRepository.countActiveClients();
    }
}
