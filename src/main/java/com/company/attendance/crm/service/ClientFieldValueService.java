package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.ClientFieldValue;
import com.company.attendance.crm.entity.ClientFieldDefinition;
import com.company.attendance.crm.repository.ClientFieldValueRepository;
import com.company.attendance.crm.repository.ClientFieldDefinitionRepository;
import com.company.attendance.entity.Client;
import com.company.attendance.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientFieldValueService {

    private final ClientFieldValueRepository fieldValueRepository;
    private final ClientFieldDefinitionRepository fieldDefinitionRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientFieldValue> getClientFieldValues(Long clientId) {
        log.debug("Fetching field values for client: {}", clientId);
        return fieldValueRepository.findByClientIdWithActiveFields(clientId);
    }

    @Transactional
    public ClientFieldValue upsertFieldValue(Long clientId, String fieldKey, String value) {
        log.debug("Upserting field value for client: {}, field: {}, value: {}", clientId, fieldKey, value);

        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));

        // Find field definition
        ClientFieldDefinition fieldDef = fieldDefinitionRepository.findByFieldKeyAndActiveTrue(fieldKey)
                .orElseThrow(() -> new RuntimeException("Field definition not found for key: " + fieldKey));

        // Check if value already exists
        return fieldValueRepository.findByClientIdAndFieldDefinitionId(clientId, fieldDef.getId())
                .map(existing -> {
                    existing.setValue(value);
                    return fieldValueRepository.save(existing);
                })
                .orElseGet(() -> {
                    ClientFieldValue newValue = ClientFieldValue.builder()
                            .client(client)
                            .fieldDefinition(fieldDef)
                            .value(value)
                            .build();
                    return fieldValueRepository.save(newValue);
                });
    }

    @Transactional
    public void deleteFieldValue(Long clientId, String fieldKey) {
        log.debug("Deleting field value for client: {}, field: {}", clientId, fieldKey);
        
        ClientFieldDefinition fieldDef = fieldDefinitionRepository.findByFieldKeyAndActiveTrue(fieldKey)
                .orElseThrow(() -> new RuntimeException("Field definition not found for key: " + fieldKey));

        fieldValueRepository.findByClientIdAndFieldDefinitionId(clientId, fieldDef.getId())
                .ifPresent(fieldValueRepository::delete);
    }

    @Transactional
    public Map<String, String> getClientFieldValuesAsMap(Long clientId) {
        log.debug("Getting field values as map for client: {}", clientId);
        
        return fieldValueRepository.findByClientIdWithActiveFields(clientId)
                .stream()
                .collect(Collectors.toMap(
                    fv -> fv.getFieldDefinition().getFieldKey(),
                    ClientFieldValue::getValue,
                    (existing, replacement) -> existing
                ));
    }

    @Transactional
    public void bulkUpdateFieldValues(Long clientId, Map<String, String> fieldValues) {
        log.debug("Bulk updating field values for client: {}", clientId);
        
        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));

        fieldValues.forEach((fieldKey, value) -> {
            try {
                upsertFieldValue(clientId, fieldKey, value);
            } catch (Exception e) {
                log.warn("Failed to update field {} for client {}: {}", fieldKey, clientId, e.getMessage());
            }
        });
    }

    @Transactional
    public void deleteAllFieldValuesForClient(Long clientId) {
        log.debug("Deleting all field values for client: {}", clientId);
        fieldValueRepository.deleteByClientId(clientId);
    }
}
