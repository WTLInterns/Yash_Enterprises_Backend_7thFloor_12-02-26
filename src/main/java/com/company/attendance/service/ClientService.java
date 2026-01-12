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

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {
    
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final CrmMapper crmMapper;

    // CRM Entity methods - using UUID only
    public List<Client> getAllClientEntities() {
        log.info("Fetching all client entities");
        return clientRepository.findAll();
    }

    public List<Client> getActiveClientEntities() {
        log.info("Fetching active client entities");
        return clientRepository.findByIsActive(true);
    }

    public Client getClientEntityById(Long id) {
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
    public Client updateClientEntity(Long id, Client client) {
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
        if (client.getIsActive() != null) {
            existingClient.setIsActive(client.getIsActive());
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

    public void deleteClientEntity(Long id) {
        log.info("Deleting client entity with ID: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        // Soft delete by setting isActive to false
        client.setIsActive(false);
        clientRepository.save(client);
        
        log.info("Client entity deleted successfully with ID: {}", id);
    }

    // Legacy DTO methods - disabled for now to avoid UUID/Long conflicts
    // Use CRM entity methods instead
}
