package com.company.attendance.service;

import com.company.attendance.dto.ClientDto;
import com.company.attendance.dto.ClientWithOwnerDto;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.Case;
import com.company.attendance.entity.Task;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Note;
import com.company.attendance.crm.entity.Activity;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.repository.CaseRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.DealProductRepository;
import com.company.attendance.crm.repository.ActivityRepository;
import com.company.attendance.crm.repository.NoteRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.mapper.ClientMapper;
import com.company.attendance.crm.mapper.CrmMapper;
import com.company.attendance.crm.service.AuditService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final CrmMapper crmMapper;
    private final DealRepository dealRepository;
    private final DealProductRepository dealProductRepository;
    private final ActivityRepository activityRepository;
    private final NoteRepository noteRepository;
    private final CaseRepository caseRepository;
    private final TaskRepository taskRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final AuditService auditService;
    private final EntityManager entityManager;
    private final GeocodingService geocodingService;
    private final EmployeeRepository employeeRepository;

    public List<Client> getAllClientEntities() {
        log.info("Fetching all client entities");
        return clientRepository.findAll();
    }

    public List<Client> getActiveClientEntities() {
        log.info("Fetching active client entities");
        List<Client> clients = clientRepository.findAllActive();
        log.info("Found {} active clients", clients.size());
        return clients;
    }

    public Client getClientEntityById(Long id) {
        log.info("Fetching client entity with ID: {}", id);
        return clientRepository.findById(id).orElse(null);
    }

    public List<Client> getClientEntitiesByIds(List<Long> ids) {
        return clientRepository.findAllById(ids);
    }

    public List<ClientWithOwnerDto> getAllClientsWithOwner() {
        log.info("Fetching all clients with owner information");
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .map(client -> {
                    ClientWithOwnerDto.OwnerInfo ownerInfo = getOwnerInfo(client.getOwnerId());
                    return ClientWithOwnerDto.fromEntity(client, ownerInfo);
                })
                .collect(Collectors.toList());
    }

    public List<ClientWithOwnerDto> getActiveClientsWithOwner() {
        log.info("Fetching active clients with owner information");
        List<Client> clients = clientRepository.findByIsActive(true);
        return clients.stream()
                .map(client -> {
                    ClientWithOwnerDto.OwnerInfo ownerInfo = getOwnerInfo(client.getOwnerId());
                    return ClientWithOwnerDto.fromEntity(client, ownerInfo);
                })
                .collect(Collectors.toList());
    }

    public ClientWithOwnerDto getClientWithOwnerById(Long id) {
        log.info("Fetching client with owner information for ID: {}", id);
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            ClientWithOwnerDto.OwnerInfo ownerInfo = getOwnerInfo(client.getOwnerId());
            return ClientWithOwnerDto.fromEntity(client, ownerInfo);
        }
        return null;
    }

    private ClientWithOwnerDto.OwnerInfo getOwnerInfo(Long ownerId) {
        if (ownerId != null) {
            return employeeRepository.findById(ownerId)
                    .map(employee -> new ClientWithOwnerDto.OwnerInfo(
                            employee.getId(),
                            employee.getFullName(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            employee.getRole() != null ? employee.getRole().getName() : "Employee"
                    ))
                    .orElse(new ClientWithOwnerDto.OwnerInfo(null, "Unknown User", "", "", "Employee"));
        }
        return new ClientWithOwnerDto.OwnerInfo(null, "Unassigned", "", "", "Employee");
    }

    public Client createClientEntity(Client client) {
        log.info("Creating new client entity: {}", client.getName());

        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + client.getEmail());
        }

        Integer currentUserId = auditService.getCurrentUserId();
        if (currentUserId != null) {
            client.setOwnerId(currentUserId.longValue());
            client.setCreatedBy(currentUserId.longValue());
        }

        if ((client.getLatitude() == null || client.getLongitude() == null) &&
            (client.getAddress() != null || client.getCity() != null)) {
            String fullAddress = geocodingService.buildFullAddress(
                client.getAddress(), client.getCity(), client.getPincode(),
                client.getState(), client.getCountry()
            );
            GeocodingService.LatLng latLng = geocodingService.geocodeAddress(fullAddress);
            if (latLng != null) {
                client.setLatitude(latLng.getLat());
                client.setLongitude(latLng.getLng());
                log.info("Auto-geocoded client {} to lat: {}, lng: {}", client.getName(), latLng.getLat(), latLng.getLng());
            }
        }

        auditService.setAuditFields(client);

        Client savedClient = clientRepository.save(client);
        log.info("Client entity created successfully with ID: {}", savedClient.getId());
        return savedClient;
    }

    @Transactional
    public Client updateClientEntity(Long id, Client client) {
        log.info("Updating client entity with ID: {}", id);

        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));

        if (client.getEmail() != null && !client.getEmail().equals(existingClient.getEmail())) {
            clientRepository.findByEmail(client.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new RuntimeException("Email already exists: " + client.getEmail());
                        }
                    });
        }

        if (client.getName() != null) existingClient.setName(client.getName());
        if (client.getEmail() != null) existingClient.setEmail(client.getEmail());
        if (client.getContactPhone() != null) existingClient.setContactPhone(client.getContactPhone());
        if (client.getAddress() != null) existingClient.setAddress(client.getAddress());
        if (client.getLatitude() != null) existingClient.setLatitude(client.getLatitude());
        if (client.getLongitude() != null) existingClient.setLongitude(client.getLongitude());
        if (client.getCity() != null) existingClient.setCity(client.getCity());
        if (client.getPincode() != null) existingClient.setPincode(client.getPincode());
        if (client.getState() != null) existingClient.setState(client.getState());
        if (client.getCountry() != null) existingClient.setCountry(client.getCountry());
        if (client.getContactName() != null) existingClient.setContactName(client.getContactName());
        if (client.getContactNumber() != null) existingClient.setContactNumber(client.getContactNumber());
        if (client.getCountryCode() != null) existingClient.setCountryCode(client.getCountryCode());
        if (client.getNotes() != null) existingClient.setNotes(client.getNotes());
        if (client.getIsActive() != null) existingClient.setIsActive(client.getIsActive());
        if (client.getCustomFields() != null) existingClient.setCustomFields(client.getCustomFields());

        Integer currentUserId = auditService.getCurrentUserId();
        if (currentUserId != null) {
            existingClient.setOwnerId(currentUserId.longValue());
            existingClient.setUpdatedBy(currentUserId.longValue());
        }

        auditService.updateAuditFields(existingClient);
        existingClient.setId(id);

        Client updatedClient = clientRepository.save(existingClient);
        log.info("Client entity updated successfully with ID: {}", updatedClient.getId());
        return updatedClient;
    }

    @Transactional
    public int bulkHardDeleteClients(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        log.info("Bulk hard deleting {} clients", ids.size());
        // Delete dependents in batch using IN queries
        taskRepository.deleteAllByClientIdIn(ids);
        entityManager.flush();
        customerAddressRepository.deleteAllByClientIdIn(ids);
        entityManager.flush();
        List<Long> dealIds = dealRepository.findAllByClientIdIn(ids)
            .stream().map(d -> d.getId().longValue()).toList();
        if (!dealIds.isEmpty()) {
            dealRepository.deleteAllById(dealIds);
            entityManager.flush();
        }
        List<com.company.attendance.entity.Case> cases = caseRepository.findAllByClientIdIn(ids);
        if (!cases.isEmpty()) {
            caseRepository.deleteAll(cases);
            entityManager.flush();
        }
        clientRepository.deleteAllById(ids);
        return ids.size();
    }

    @Transactional
    public void hardDeleteClientEntity(Long id) {
        log.info("Hard deleting client entity with ID: {}", id);
        // 1. Delete tasks linked to this client
        List<Task> tasks = taskRepository.findByClientId(id);
        if (!tasks.isEmpty()) {
            taskRepository.deleteAll(tasks);
            entityManager.flush();
            entityManager.clear();
        }
        // 2. Delete customer addresses
        customerAddressRepository.deleteByClientId(id);
        entityManager.flush();
        entityManager.clear();
        // 3. Delete deals (cascade handles stageHistory, dealProducts, notes, activities)
        List<Deal> deals = dealRepository.findByClientId(id);
        if (!deals.isEmpty()) {
            dealRepository.deleteAll(deals);
            entityManager.flush();
            entityManager.clear();
        }
        // 4. Delete cases
        List<Case> cases = caseRepository.findByClientId(id);
        if (!cases.isEmpty()) {
            caseRepository.deleteAll(cases);
            entityManager.flush();
            entityManager.clear();
        }
        // 5. Physical delete
        clientRepository.deleteById(id);
        log.info("Client hard deleted with ID: {}", id);
    }

    @Transactional
    public void deleteClientEntity(Long id) {
        log.info("Deleting client entity with ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));

        List<Deal> deals = dealRepository.findByClientId(id);
        if (!deals.isEmpty()) {
            dealRepository.deleteAll(deals);
            entityManager.flush();
            entityManager.clear();
        }

        List<Case> cases = caseRepository.findByClientId(id);
        if (!cases.isEmpty()) {
            caseRepository.deleteAll(cases);
            entityManager.flush();
            entityManager.clear();
        }

        client.setIsActive(false);
        clientRepository.save(client);
        log.info("Client deleted successfully (soft) with ID: {}", id);
    }
}
