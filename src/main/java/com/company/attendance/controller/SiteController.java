package com.company.attendance.controller;

import com.company.attendance.dto.SiteRequestDto;
import com.company.attendance.dto.SiteResponseDto;
import com.company.attendance.dto.SiteImportResultDto;
import com.company.attendance.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    public ResponseEntity<SiteResponseDto> createSite(@Valid @RequestBody SiteRequestDto requestDto) {
        SiteResponseDto created = siteService.createSite(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SiteResponseDto>> getAllSites() {
        List<SiteResponseDto> sites = siteService.getAllSites();
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteResponseDto> getSiteById(@PathVariable Long id) {
        SiteResponseDto site = siteService.getSiteById(id);
        return ResponseEntity.ok(site);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteResponseDto> updateSite(@PathVariable Long id,
                                                      @Valid @RequestBody SiteRequestDto requestDto) {
        SiteResponseDto updated = siteService.updateSite(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDeleteSites(@RequestBody List<Long> ids) {
        siteService.deleteMultipleSites(ids);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SiteImportResultDto> importSites(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            SiteImportResultDto result = siteService.importSitesFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Sites Excel import failed", ex);
            SiteImportResultDto fallback = SiteImportResultDto.builder()
                    .totalRows(0)
                    .successCount(0)
                    .failedCount(0)
                    .build();
            return ResponseEntity.ok(fallback);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportSites() {
        byte[] data = siteService.exportSitesToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sites.xlsx");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
