package com.company.attendance.service;

import com.company.attendance.dto.CaseDto;
import com.company.attendance.entity.Case;
import com.company.attendance.entity.Client;
import com.company.attendance.mapper.CaseMapper;
import com.company.attendance.mapper.ClientMapper;
import com.company.attendance.repository.CaseRepository;
import com.company.attendance.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CaseService {

    private final CaseRepository caseRepository;
    private final ClientRepository clientRepository;
    private final CaseMapper caseMapper;
    private final ClientMapper clientMapper;

    public CaseDto createCase(CaseDto caseDto) {
        Case entity = caseMapper.toEntity(caseDto);

        // Set client relation from clientId (UUID)
        if (caseDto.getClientId() != null) {
            UUID clientId = caseDto.getClientId();
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
            entity.setClient(client);
        }
        Case saved = caseRepository.save(entity);
        return caseMapper.toDto(saved);
    }

    public CaseDto updateCase(Long id, CaseDto caseDto) {
        Case existing = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        caseMapper.updateEntityFromDto(caseDto, existing);

        // Update client relation if a clientId is provided (UUID)
        if (caseDto.getClientId() != null) {
            UUID clientId = caseDto.getClientId();
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
            existing.setClient(client);
        }
        Case updated = caseRepository.save(existing);
        return caseMapper.toDto(updated);
    }

    public CaseDto getCaseById(Long id) {
        Case entity = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        return caseMapper.toDto(entity);
    }

    public List<CaseDto> getAllCases() {
        List<Case> cases = caseRepository.findAll();
        return caseMapper.toDtoList(cases);
    }

    public List<CaseDto> getCasesByClientId(UUID clientId) {
        List<Case> cases = caseRepository.findByClientId(clientId);
        return caseMapper.toDtoList(cases);
    }

    public List<CaseDto> searchCases(String search) {
        List<Case> allCases = caseRepository.findAll();
        return allCases.stream()
                .filter(c -> {
                    String title = c.getTitle() != null ? c.getTitle().toLowerCase() : "";
                    String number = c.getCaseNumber() != null ? c.getCaseNumber().toLowerCase() : "";
                    String term = search.toLowerCase();
                    return title.contains(term) || number.contains(term);
                })
                .map(caseMapper::toDto)
                .toList();
    }

    public void deleteCase(Long id) {
        Case entity = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        caseRepository.delete(entity);
    }
}
