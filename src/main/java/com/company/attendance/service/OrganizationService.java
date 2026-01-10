package com.company.attendance.service;

import com.company.attendance.dto.OrganizationDto;
import com.company.attendance.entity.Organization;
import com.company.attendance.exception.BadRequestException;
import com.company.attendance.exception.ResourceNotFoundException;
import com.company.attendance.mapper.OrganizationMapper;
import com.company.attendance.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Transactional(readOnly = true)
    public Page<OrganizationDto> getAllOrganizations(Pageable pageable) {
        log.debug("Fetching all organizations with pagination - {}", pageable);
        return organizationRepository.findAll(pageable)
                .map(organizationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationById(Long id) {
        log.debug("Fetching organization with id: {}", id);
        return organizationRepository.findById(id)
                .map(organizationMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationByCode(String code) {
        log.debug("Fetching organization with code: {}", code);
        return organizationRepository.findByCode(code)
                .map(organizationMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with code: " + code));
    }

    @Transactional
    public OrganizationDto createOrganization(OrganizationDto organizationDto) {
        log.debug("Creating new organization: {}", organizationDto);
        
        // Check if organization with same code already exists
        if (organizationRepository.existsByCode(organizationDto.getCode())) {
            throw new BadRequestException("Organization with code " + organizationDto.getCode() + " already exists");
        }

        // Check if email is already in use
        if (organizationRepository.existsByContactEmailAndIdNot(organizationDto.getContactEmail(), 0L)) {
            throw new BadRequestException("Email " + organizationDto.getContactEmail() + " is already in use");
        }

        Organization organization = organizationMapper.toEntity(organizationDto);
        organization.setIsActive(true);
        
        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Created new organization with id: {}", savedOrganization.getId());
        
        return organizationMapper.toDto(savedOrganization);
    }

    @Transactional
    public OrganizationDto updateOrganization(Long id, OrganizationDto organizationDto) {
        log.debug("Updating organization with id: {}, data: {}", id, organizationDto);
        
        Organization existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        // Check if the new code is already taken by another organization
        if (!existingOrganization.getCode().equalsIgnoreCase(organizationDto.getCode()) && 
            organizationRepository.existsByCode(organizationDto.getCode())) {
            throw new BadRequestException("Organization with code " + organizationDto.getCode() + " already exists");
        }

        // Check if the new email is already in use by another organization
        if (!existingOrganization.getContactEmail().equalsIgnoreCase(organizationDto.getContactEmail()) && 
            organizationRepository.existsByContactEmailAndIdNot(organizationDto.getContactEmail(), id)) {
            throw new BadRequestException("Email " + organizationDto.getContactEmail() + " is already in use by another organization");
        }

        // Update the organization with new data
        organizationMapper.updateOrganizationFromDto(organizationDto, existingOrganization);
        
        Organization updatedOrganization = organizationRepository.save(existingOrganization);
        log.info("Updated organization with id: {}", id);
        
        return organizationMapper.toDto(updatedOrganization);
    }

    @Transactional
    public void deleteOrganization(Long id) {
        log.debug("Deleting organization with id: {}", id);
        
        if (!organizationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Organization not found with id: " + id);
        }
        
        organizationRepository.deleteById(id);
        log.info("Deleted organization with id: {}", id);
    }

    @Transactional
    public OrganizationDto activateOrganization(Long id, boolean active) {
        log.debug("Setting active status to {} for organization id: {}", active, id);
        
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        
        organization.setIsActive(active);
        Organization updatedOrganization = organizationRepository.save(organization);
        
        log.info("Organization with id: {} is now {}", id, active ? "active" : "inactive");
        return organizationMapper.toDto(updatedOrganization);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationDto> searchOrganizations(
            String name, String code, String industry, Boolean isActive, Pageable pageable) {
        log.debug("Searching organizations with name: {}, code: {}, industry: {}, isActive: {}", 
                name, code, industry, isActive);
                
        return organizationRepository.searchOrganizations(
                name, code, industry, isActive, pageable)
                .map(organizationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Organization findByName(String name) {
        log.debug("Finding organization by name: {}", name);
        return organizationRepository.findByName(name).orElse(null);
    }

    @Transactional
    public Organization findByNameOrCreate(String name) {
        log.debug("Finding or creating organization: {}", name);
        Optional<Organization> existingOrgOpt = organizationRepository.findByName(name);
        if (existingOrgOpt.isPresent()) {
            return existingOrgOpt.get();
        }
        
        // Create new organization if not found
        Organization newOrg = Organization.builder()
                .name(name)
                .code("ORG_" + System.currentTimeMillis())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(0L) // Use Long instead of String
                .build();
        
        return organizationRepository.save(newOrg);
    }

    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        log.debug("Finding all organizations");
        return organizationRepository.findAll();
    }
}
