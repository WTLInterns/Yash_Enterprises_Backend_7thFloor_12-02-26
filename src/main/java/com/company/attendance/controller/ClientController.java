package com.company.attendance.controller;

import com.company.attendance.crm.dto.DealDetailDTO;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.dto.ClientDto;
import com.company.attendance.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;
    private final DealRepository dealRepository;

    @GetMapping
    public ResponseEntity<List<ClientDto>> listClients() {
        log.info("GET /api/clients - Fetching all clients");
        try {
            List<ClientDto> clients = clientService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            log.error("Error fetching clients: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<ClientDto>> getActiveClients() {
        log.info("GET /api/clients/active - Fetching active clients");
        try {
            List<ClientDto> clients = clientService.getActiveClients();
            return ResponseEntity.ok(clients);
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
    public ResponseEntity<ClientDto> getClient(@PathVariable Long id) {
        log.info("GET /api/clients/{} - Fetching client", id);
        try {
            ClientDto client = clientService.getClientById(id);
            return ResponseEntity.ok(client);
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
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientDto clientDto) {
        log.info("POST /api/clients - Creating new client: {}", clientDto.getName());
        try {
            ClientDto createdClient = clientService.createClient(clientDto);
            return ResponseEntity.status(201).body(createdClient);
        } catch (Exception e) {
            log.error("Error creating client: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDto clientDto) {
        log.info("PUT /api/clients/{} - Updating client", id);
        try {
            ClientDto updatedClient = clientService.updateClient(id, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (Exception e) {
            log.error("Error updating client: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        log.info("DELETE /api/clients/{} - Deleting client", id);
        try {
            clientService.deleteClient(id);
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
