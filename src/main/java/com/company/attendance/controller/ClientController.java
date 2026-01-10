package com.company.attendance.controller;

import com.company.attendance.crm.dto.DealDetailDTO;
import com.company.attendance.crm.dto.ClientDto as CrmClientDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.Client;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.dto.ClientDto;
import com.company.attendance.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;
    private final DealRepository dealRepository;
    private final CrmMapper crmMapper;

    @GetMapping
    public ResponseEntity<List<CrmClientDto>> listClients() {
        log.info("GET /api/clients - Fetching all clients");
        try {
            List<Client> clients = clientService.getAllClientEntities();
            List<CrmClientDto> dtos = clients.stream()
                .map(crmMapper::toClientDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error fetching clients: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<CrmClientDto>> getActiveClients() {
        log.info("GET /api/clients/active - Fetching active clients");
        try {
            List<Client> clients = clientService.getActiveClientEntities();
            List<CrmClientDto> dtos = clients.stream()
                .map(crmMapper::toClientDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error fetching active clients: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> searchClients(@RequestParam String search) {
        log.info("GET /api/clients/search - Searching clients with term: {}", search);
        try {
            List<ClientDto> clients = clientService.searchClients(search);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            log.error("Error searching clients: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CrmClientDto> getClient(@PathVariable UUID id) {
        log.info("GET /api/clients/{} - Fetching client", id);
        try {
            Client client = clientService.getClientEntityById(id);
            if (client == null) {
                return ResponseEntity.notFound().build();
            }
            CrmClientDto clientDto = crmMapper.toClientDto(client);
            return ResponseEntity.ok(clientDto);
        } catch (Exception e) {
            log.error("Error fetching client: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/deal")
    public ResponseEntity<DealDetailDTO> getClientDeal(@PathVariable Long id) {
        log.info("GET /api/clients/{}/deal - Fetching latest deal for client", id);
        Deal deal = dealRepository.findFirstByClientIdOrderByCreatedAtDesc(id).orElse(null);
        if (deal == null) return ResponseEntity.notFound().build();
        DealDetailDTO dto = new DealDetailDTO();
        dto.id = deal.getId();
        dto.name = deal.getName();
        dto.valueAmount = deal.getValueAmount();
        dto.closingDate = deal.getClosingDate();
        dto.stage = deal.getStage() != null ? deal.getStage().name() : null;
        dto.notesCount = 0;
        dto.activitiesCount = 0;
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<CrmClientDto> createClient(@Valid @RequestBody CrmClientDto clientDto) {
        log.info("POST /api/clients - Creating new client: {}", clientDto.getName());
        try {
            Client client = crmMapper.toClientEntity(clientDto);
            Client createdClient = clientService.createClientEntity(client);
            CrmClientDto response = crmMapper.toClientDto(createdClient);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            log.error("Error creating client: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CrmClientDto> updateClient(@PathVariable UUID id, @Valid @RequestBody CrmClientDto clientDto) {
        log.info("PUT /api/clients/{} - Updating client", id);
        try {
            Client client = crmMapper.toClientEntity(clientDto);
            Client updatedClient = clientService.updateClientEntity(id, client);
            CrmClientDto response = crmMapper.toClientDto(updatedClient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating client: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        log.info("DELETE /api/clients/{} - Deleting client", id);
        try {
            clientService.deleteClientEntity(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting client: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getActiveClientsCount() {
        log.info("GET /api/clients/count - Getting active clients count");
        try {
            long count = clientService.getActiveClientsCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting clients count: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
