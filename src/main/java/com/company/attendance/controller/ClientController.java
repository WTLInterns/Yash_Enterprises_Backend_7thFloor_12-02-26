package com.company.attendance.controller;

import com.company.attendance.crm.dto.ClientWithDealsDto;
import com.company.attendance.crm.dto.DealDetailDTO;
import com.company.attendance.crm.entity.Bank;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.repository.BankRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.service.AuditService;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.ExpenseRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.TaskRepository;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
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
    private final EmployeeRepository employeeRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final BankRepository bankRepository;
    private final ExpenseRepository expenseRepository;
    private final TaskRepository taskRepository;

    @GetMapping("/assigned")
    @Transactional(readOnly = true)
    public ResponseEntity<List<com.company.attendance.crm.dto.ClientDto>> listAssignedClients(
            @RequestParam(value = "employeeId", required = false) Long employeeIdParam,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader) {
        final long startedNs = System.nanoTime();
        try {
            final String role = userRoleHeader != null ? userRoleHeader : "";
            Long employeeId = employeeIdParam;
            if (employeeId == null && userIdHeader != null) {
                try {
                    employeeId = Long.valueOf(userIdHeader);
                } catch (NumberFormatException ignored) {
                    employeeId = null;
                }
            }

            if (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("MANAGER")) {
                List<Client> clients = clientService.getAllClientEntities().stream()
                        .filter(c -> c.getIsActive() == null || c.getIsActive())
                        .collect(Collectors.toList());

                List<Long> ownerIds = clients.stream()
                        .map(Client::getOwnerId)
                        .filter(Objects::nonNull)
                        .distinct().toList();

                Map<Long, String> ownerNames = new HashMap<>();
                if (!ownerIds.isEmpty()) {
                    employeeRepository.findAllById(ownerIds)
                            .forEach(e -> ownerNames.put(e.getId(), e.getFullName()));
                }

                List<com.company.attendance.crm.dto.ClientDto> dtos = clients.stream()
                        .map(c -> {
                            com.company.attendance.crm.dto.ClientDto dto = crmMapper.toClientDto(c);
                            if (c.getOwnerId() != null) dto.setOwnerName(ownerNames.get(c.getOwnerId()));
                            return dto;
                        })
                        .collect(Collectors.toList());

                final long tookMs = (System.nanoTime() - startedNs) / 1_000_000;
                log.info("[AssignedClientsAPI] role={} employeeId={} matchedTasks={} uniqueClients={} responseSize={} tookMs={}",
                        role, employeeId, -1, dtos.size(), dtos.size(), tookMs);
                return ResponseEntity.ok(dtos);
            }

            if (employeeId == null) {
                final long tookMs = (System.nanoTime() - startedNs) / 1_000_000;
                log.warn("[AssignedClientsAPI] missing employeeId role={} userIdHeader={} employeeIdParam={} tookMs={}",
                        role, userIdHeader, employeeIdParam, tookMs);
                return ResponseEntity.badRequest().body(List.of());
            }

            final long matchedTasks = taskRepository.countByAssignedToEmployeeIdAndClientIdIsNotNull(employeeId);
            List<Client> assigned = clientService.getDistinctActiveClientsAssignedToEmployee(employeeId);

            List<Long> ownerIds = assigned.stream()
                    .map(Client::getOwnerId)
                    .filter(Objects::nonNull)
                    .distinct().toList();
            Map<Long, String> ownerNames = new HashMap<>();
            if (!ownerIds.isEmpty()) {
                employeeRepository.findAllById(ownerIds)
                        .forEach(e -> ownerNames.put(e.getId(), e.getFullName()));
            }

            List<com.company.attendance.crm.dto.ClientDto> dtos = assigned.stream()
                    .map(c -> {
                        com.company.attendance.crm.dto.ClientDto dto = crmMapper.toClientDto(c);
                        if (c.getOwnerId() != null) dto.setOwnerName(ownerNames.get(c.getOwnerId()));
                        return dto;
                    })
                    .collect(Collectors.toList());

            final long tookMs = (System.nanoTime() - startedNs) / 1_000_000;
            log.info("[AssignedClientsAPI] role={} employeeId={} matchedTasks={} uniqueClients={} responseSize={} tookMs={}",
                    role, employeeId, matchedTasks, dtos.size(), dtos.size(), tookMs);

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            final long tookMs = (System.nanoTime() - startedNs) / 1_000_000;
            log.error("[AssignedClientsAPI] error role={} employeeIdParam={} userIdHeader={} tookMs={} err={}",
                    userRoleHeader, employeeIdParam, userIdHeader, tookMs, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Single endpoint: returns all clients + all their deals + all addresses.
     * Replaces 3 separate API calls (clients + deals + addresses) on the frontend.
     * Uses batch queries — no N+1.
     */
    @GetMapping("/with-deals")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ClientWithDealsDto>> listClientsWithDeals() {
        try {
            // 1. All active clients
            List<Client> clients = clientService.getAllClientEntities().stream()
                .filter(c -> c.getIsActive() == null || c.getIsActive())
                .collect(Collectors.toList());

            if (clients.isEmpty()) return ResponseEntity.ok(List.of());

            List<Long> clientIds = clients.stream().map(Client::getId).toList();

            // 2. Batch-load owner names
            List<Long> ownerIds = clients.stream().map(Client::getOwnerId)
                .filter(Objects::nonNull).distinct().toList();
            Map<Long, String> ownerNames = new HashMap<>();
            if (!ownerIds.isEmpty()) {
                employeeRepository.findAllById(ownerIds)
                    .forEach(e -> ownerNames.put(e.getId(), e.getFullName()));
            }

            // 3. Batch-load ALL deals with products eagerly fetched (avoids LazyInitializationException)
            List<Deal> allDeals = dealRepository.findAllWithClientAndProductsFull().stream()
                .filter(d -> clientIds.contains(d.getClientId()))
                .collect(Collectors.toList());

            // 4. Batch-load ALL banks referenced by deals
            List<Long> bankIds = allDeals.stream().map(Deal::getBankId)
                .filter(Objects::nonNull).distinct().toList();
            Map<Long, Bank> banksMap = new HashMap<>();
            if (!bankIds.isEmpty()) {
                bankRepository.findAllById(bankIds).forEach(b -> banksMap.put(b.getId(), b));
            }

            // 5. Batch-load ALL addresses
            List<CustomerAddress> allAddresses = customerAddressRepository.findAllByClientIdIn(clientIds);
            Map<Long, List<CustomerAddress>> addressesByClient = allAddresses.stream()
                .collect(Collectors.groupingBy(CustomerAddress::getClientId));

            // 5b. Batch-load ALL expenses by clientId for calculatedValue
            List<com.company.attendance.entity.Expense> allExpenses =
                expenseRepository.findAllByClientIdIn(clientIds);
            Map<Long, java.math.BigDecimal> expenseTotalByDeal = new HashMap<>();
            allExpenses.forEach(e -> {
                if (e.getDealId() != null && e.getAmount() != null) {
                    expenseTotalByDeal.merge(e.getDealId(),
                        java.math.BigDecimal.valueOf(e.getAmount()),
                        java.math.BigDecimal::add);
                }
            });

            // 6. Group deals by clientId
            Map<Long, List<Deal>> dealsByClient = allDeals.stream()
                .collect(Collectors.groupingBy(Deal::getClientId));

            // 7. Assemble DTOs
            List<ClientWithDealsDto> result = clients.stream().map(c -> {
                ClientWithDealsDto dto = new ClientWithDealsDto();
                dto.setId(c.getId());
                dto.setName(c.getName());
                dto.setEmail(c.getEmail());
                dto.setContactPhone(c.getContactPhone());
                dto.setContactName(c.getContactName());
                dto.setContactNumber(c.getContactNumber());
                dto.setOwnerId(c.getOwnerId());
                dto.setOwnerName(c.getOwnerId() != null ? ownerNames.get(c.getOwnerId()) : null);
                dto.setCreatedAt(c.getCreatedAt());
                dto.setUpdatedAt(c.getUpdatedAt());

                // Addresses
                List<CustomerAddress> addrs = addressesByClient.getOrDefault(c.getId(), List.of());
                dto.setAddresses(addrs.stream().map(a -> {
                    ClientWithDealsDto.AddressDto ad = new ClientWithDealsDto.AddressDto();
                    ad.setId(a.getId());
                    ad.setAddressType(a.getAddressType() != null ? a.getAddressType().name() : null);
                    ad.setAddressLine(a.getAddressLine());
                    ad.setCity(a.getCity());
                    ad.setState(a.getState());
                    ad.setPincode(a.getPincode());
                    ad.setTaluka(a.getTaluka());
                    ad.setDistrict(a.getDistrict());
                    ad.setLatitude(a.getLatitude());
                    ad.setLongitude(a.getLongitude());
                    ad.setIsPrimary(a.getIsPrimary());
                    return ad;
                }).collect(Collectors.toList()));

                // Deals
                List<Deal> clientDeals = dealsByClient.getOrDefault(c.getId(), List.of());
                dto.setDeals(clientDeals.stream().map(d -> {
                    ClientWithDealsDto.DealSummaryDto dd = new ClientWithDealsDto.DealSummaryDto();
                    dd.setId(d.getId());
                    dd.setDealCode(d.getDealCode());
                    dd.setStageCode(d.getStageCode());
                    dd.setDepartment(d.getDepartment());
                    dd.setBankId(d.getBankId());
                    dd.setBranchName(d.getBranchName());
                    dd.setValueAmount(d.getValueAmount());
                    // calculatedValue = sum(products) - sum(expenses), same as /customers/[id] finalAmount
                    java.math.BigDecimal productTotal = java.math.BigDecimal.ZERO;
                    if (d.getDealProducts() != null) {
                        for (var dp : d.getDealProducts()) {
                            if (dp.getTotal() != null) productTotal = productTotal.add(dp.getTotal());
                        }
                    }
                    java.math.BigDecimal expenseTotal = expenseTotalByDeal.getOrDefault(d.getId(), java.math.BigDecimal.ZERO);
                    java.math.BigDecimal computed = productTotal.subtract(expenseTotal);
                    // Use computed if products exist, else fall back to valueAmount
                    dd.setCalculatedValue(productTotal.compareTo(java.math.BigDecimal.ZERO) > 0
                        ? computed
                        : d.getValueAmount());
                    dd.setClosingDate(d.getClosingDate());
                    dd.setDescription(d.getDescription());
                    dd.setMovedToApproval(d.getMovedToApproval());
                    dd.setCreatedAt(d.getCreatedAt());
                    dd.setUpdatedAt(d.getUpdatedAt());
                    // Bank details
                    Bank bank = d.getBankId() != null ? banksMap.get(d.getBankId()) : null;
                    dd.setBankName(d.getRelatedBankName() != null ? d.getRelatedBankName() : (bank != null ? bank.getName() : null));
                    dd.setTaluka(bank != null ? bank.getTaluka() : null);
                    dd.setDistrict(bank != null ? bank.getDistrict() : null);
                    // Product names
                    if (d.getDealProducts() != null) {
                        dd.setProductNames(d.getDealProducts().stream()
                            .filter(dp -> dp.getProduct() != null)
                            .map(dp -> dp.getProduct().getName())
                            .collect(Collectors.toList()));
                    } else {
                        dd.setProductNames(List.of());
                    }
                    return dd;
                }).collect(Collectors.toList()));

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in /clients/with-deals: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<com.company.attendance.crm.dto.ClientDto>> listClients(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String stage) {
        try {
            List<Client> clients;

            if (department != null || stage != null) {
                List<Deal> deals = (department != null && stage != null)
                    ? dealRepository.findByDepartmentAndStage(department, stage)
                    : department != null
                        ? dealRepository.findByDepartment(department)
                        : dealRepository.findAll();

                List<Long> clientIds = deals.stream().map(Deal::getClientId).distinct().toList();
                clients = clientIds.isEmpty() ? List.of()
                    : clientService.getClientEntitiesByIds(clientIds).stream()
                        .filter(c -> c.getIsActive() == null || c.getIsActive())
                        .toList();
            } else {
                // Return all clients that have at least one deal (active or not)
                // This prevents soft-deleted+re-imported clients from disappearing
                clients = clientService.getAllClientEntities().stream()
                    .filter(c -> c.getIsActive() == null || c.getIsActive())
                    .collect(Collectors.toList());
            }

            // Batch-load owner names — one query for all unique ownerIds
            List<Long> ownerIds = clients.stream()
                .map(Client::getOwnerId)
                .filter(java.util.Objects::nonNull)
                .distinct().toList();
            java.util.Map<Long, String> ownerNames = new java.util.HashMap<>();
            if (!ownerIds.isEmpty()) {
                employeeRepository.findAllById(ownerIds)
                    .forEach(e -> ownerNames.put(e.getId(), e.getFullName()));
            }

            List<com.company.attendance.crm.dto.ClientDto> dtos = clients.stream()
                .map(c -> {
                    com.company.attendance.crm.dto.ClientDto dto = crmMapper.toClientDto(c);
                    if (c.getOwnerId() != null) dto.setOwnerName(ownerNames.get(c.getOwnerId()));
                    return dto;
                })
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
        Client client = clientService.getClientEntityById(id);
        if (client == null) {
            log.warn("Client not found with id={}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(crmMapper.toClientDto(client));
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
        dto.stage = deal.getStageCode();
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
                clientDto.setCreatedBy(auditService.getCurrentUserId() != null ? auditService.getCurrentUserId().longValue() : null);
            }
            if (clientDto.getOwnerId() == null) {
                clientDto.setOwnerId(auditService.getCurrentUserId() != null ? auditService.getCurrentUserId().longValue() : null);
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
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                log.error("Client not found: {}", e.getMessage());
                return ResponseEntity.notFound().build();
            } else {
                log.error("Error deleting client: {}", e.getMessage());
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            log.error("Error deleting client: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/bulk")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<java.util.Map<String, Object>> bulkDeleteClients(@RequestBody java.util.Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "No IDs provided"));
        }
        log.info("DELETE /api/clients/bulk - Hard deleting {} clients", ids.size());
        int deleted = clientService.bulkHardDeleteClients(ids);
        return ResponseEntity.ok(java.util.Map.of("deleted", deleted, "message", deleted + " client(s) deleted successfully"));
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
