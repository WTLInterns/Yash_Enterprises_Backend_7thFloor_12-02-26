package com.company.attendance.controller;

import com.company.attendance.crm.dto.DealDetailDTO;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.entity.Client;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.service.AuditService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;
    private final DealRepository dealRepository;
    private final CrmMapper crmMapper;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<com.company.attendance.crm.dto.ClientDto>> listClients() {
        log.info("GET /api/clients - Fetching all clients");
        try {
            List<Client> clients = clientService.getAllClientEntities();
            List<com.company.attendance.crm.dto.ClientDto> dtos = clients.stream()
                .map(crmMapper::toClientDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error fetching clients: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<com.company.attendance.crm.dto.ClientDto>> getActiveClients() {
        log.info("GET /api/clients/active - Fetching active clients");
        try {
            List<Client> clients = clientService.getActiveClientEntities();
            List<com.company.attendance.crm.dto.ClientDto> dtos = clients.stream()
                .map(crmMapper::toClientDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error fetching active clients: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<com.company.attendance.crm.dto.ClientDto>> searchClients(@RequestParam String search) {
        log.info("GET /api/clients/search - Searching clients with term: {}", search);
        try {
            // Simple search implementation
            List<Client> allClients = clientService.getAllClientEntities();
            List<com.company.attendance.crm.dto.ClientDto> filtered = allClients.stream()
                .filter(client -> client.getName().toLowerCase().contains(search.toLowerCase()) ||
                               (client.getEmail() != null && client.getEmail().toLowerCase().contains(search.toLowerCase())))
                .map(crmMapper::toClientDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            log.error("Error searching clients: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<com.company.attendance.crm.dto.ClientDto> getClient(@PathVariable Long id) {
        log.info("GET /api/clients/{} - Fetching client", id);
        try {
            Client client = clientService.getClientEntityById(id);
            if (client == null) {
                return ResponseEntity.notFound().build();
            }
            com.company.attendance.crm.dto.ClientDto clientDto = crmMapper.toClientDto(client);
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
        dto.id = deal.getId() != null ? deal.getId().longValue() : null;
        dto.name = deal.getName();
        dto.valueAmount = deal.getValueAmount();
        dto.closingDate = deal.getClosingDate();
        dto.stage = deal.getStage() != null ? deal.getStage().name() : null;
        dto.notesCount = 0;
        dto.activitiesCount = 0;
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<com.company.attendance.crm.dto.ClientDto> createClient(@Valid @RequestBody com.company.attendance.crm.dto.ClientDto clientDto) {
        log.info("POST /api/clients - Creating new client: {}", clientDto.getName());
        try {
            // Auto-set owner fields from authenticated user
            if (clientDto.getCreatedBy() == null) {
                clientDto.setCreatedBy(auditService.getCurrentUserId());
            }
            if (clientDto.getOwnerId() == null) {
                clientDto.setOwnerId(auditService.getCurrentUserId());
            }
            
            Client client = crmMapper.toClientEntity(clientDto);
            Client createdClient = clientService.createClientEntity(client);
            com.company.attendance.crm.dto.ClientDto response = crmMapper.toClientDto(createdClient);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            log.error("Error creating client: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<com.company.attendance.crm.dto.ClientDto> updateClient(@PathVariable Long id, @Valid @RequestBody com.company.attendance.crm.dto.ClientDto clientDto) {
        log.info("PUT /api/clients/{} - Updating client", id);
        try {
            Client client = crmMapper.toClientEntity(clientDto);
            Client updatedClient = clientService.updateClientEntity(id, client);
            com.company.attendance.crm.dto.ClientDto response = crmMapper.toClientDto(updatedClient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating client: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
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
            long count = clientService.getActiveClientEntities().size();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting clients count: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
