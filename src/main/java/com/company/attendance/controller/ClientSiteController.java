package com.company.attendance.controller;

import com.company.attendance.dto.SiteRequestDto;
import com.company.attendance.dto.SiteResponseDto;
import com.company.attendance.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/sites")
@RequiredArgsConstructor
@Slf4j
public class ClientSiteController {

    private final SiteService siteService;

    @GetMapping
    public ResponseEntity<List<SiteResponseDto>> getSitesByClientId(@PathVariable Long clientId) {
        log.info("GET /api/clients/{}/sites - Fetching sites for client", clientId);
        List<SiteResponseDto> sites = siteService.getSitesByClientId(clientId);
        return ResponseEntity.ok(sites);
    }

    @PostMapping
    public ResponseEntity<SiteResponseDto> createSiteForClient(
            @PathVariable Long clientId,
            @Valid @RequestBody SiteRequestDto requestDto) {
        
        log.info("POST /api/clients/{}/sites - Creating site for client", clientId);
        
        // Set clientId from path variable
        requestDto.setClientId(clientId);
        
        SiteResponseDto created = siteService.createSite(requestDto);
        return ResponseEntity.created(URI.create("/api/sites/" + created.getId())).body(created);
    }

    @PutMapping("/{siteId}")
    public ResponseEntity<SiteResponseDto> updateSite(
            @PathVariable Long clientId,
            @PathVariable Long siteId,
            @Valid @RequestBody SiteRequestDto requestDto) {
        
        log.info("PUT /api/clients/{}/sites/{} - Updating site", clientId, siteId);
        
        // Ensure clientId matches
        requestDto.setClientId(clientId);
        
        SiteResponseDto updated = siteService.updateSite(siteId, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{siteId}")
    public ResponseEntity<Void> deleteSite(
            @PathVariable Long clientId,
            @PathVariable Long siteId) {
        
        log.info("DELETE /api/clients/{}/sites/{} - Deleting site", clientId, siteId);
        siteService.deleteSite(siteId);
        return ResponseEntity.noContent().build();
    }
}
