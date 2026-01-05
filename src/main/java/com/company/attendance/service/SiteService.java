package com.company.attendance.service;

import com.company.attendance.dto.SiteRequestDto;
import com.company.attendance.dto.SiteResponseDto;
import com.company.attendance.dto.SiteImportResultDto;
import com.company.attendance.entity.Site;
import com.company.attendance.exception.BadRequestException;
import com.company.attendance.exception.ResourceNotFoundException;
import com.company.attendance.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional
    public SiteResponseDto createSite(SiteRequestDto requestDto) {
        log.debug("Creating new site: {}", requestDto);
        Site site = mapToEntity(new Site(), requestDto);
        Site saved = siteRepository.save(site);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SiteResponseDto> getAllSites() {
        log.debug("Fetching all sites");
        return siteRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportSitesToExcel() {
        log.debug("Exporting all sites to Excel");

        List<Site> sites = siteRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sites");

            // Header style (bold)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = new String[]{
                    "Site",
                    "Site ID",
                    "Address",
                    "Email",
                    "Description",
                    "Contact Person",
                    "Contact Number",
                    "Latitude",
                    "Longitude",
                    "City",
                    "Pincode"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (Site site : sites) {
                Row row = sheet.createRow(rowIdx++);

                int col = 0;
                row.createCell(col++).setCellValue(site.getSiteName() != null ? site.getSiteName() : "");
                row.createCell(col++).setCellValue(site.getSiteId() != null ? site.getSiteId() : "");
                row.createCell(col++).setCellValue(site.getAddress() != null ? site.getAddress() : "");
                row.createCell(col++).setCellValue(site.getEmail() != null ? site.getEmail() : "");
                row.createCell(col++).setCellValue(site.getDescription() != null ? site.getDescription() : "");
                row.createCell(col++).setCellValue(site.getContactPerson() != null ? site.getContactPerson() : "");
                row.createCell(col++).setCellValue(site.getContactNumber() != null ? site.getContactNumber() : "");

                Cell latCell = row.createCell(col++);
                if (site.getLatitude() != null) {
                    latCell.setCellValue(site.getLatitude());
                }

                Cell lngCell = row.createCell(col++);
                if (site.getLongitude() != null) {
                    lngCell.setCellValue(site.getLongitude());
                }

                row.createCell(col++).setCellValue(site.getCity() != null ? site.getCity() : "");
                row.createCell(col).setCellValue(site.getPincode() != null ? site.getPincode() : "");
            }

            // Auto-size columns for readability
            for (int i = 0; i < 11; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Failed to export sites to Excel", ex);
            throw new BadRequestException("Unable to export sites to Excel: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SiteResponseDto getSiteById(Long id) {
        log.debug("Fetching site with id: {}", id);
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));
        return mapToResponseDto(site);
    }

    @Transactional
    public SiteResponseDto updateSite(Long id, SiteRequestDto requestDto) {
        log.debug("Updating site with id: {}, data: {}", id, requestDto);
        Site existing = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));

        existing = mapToEntity(existing, requestDto);
        Site updated = siteRepository.save(existing);
        return mapToResponseDto(updated);
    }

    @Transactional
    public void deleteSite(Long id) {
        log.debug("Deleting site with id: {}", id);
        if (!siteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Site not found with id: " + id);
        }
        siteRepository.deleteById(id);
    }

    @Transactional
    public void deleteMultipleSites(List<Long> ids) {
        log.debug("Bulk deleting sites with ids: {}", ids);
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("No site IDs provided for bulk deletion");
        }
        siteRepository.deleteAllByIdInBatch(ids);
    }

    @Transactional
    public SiteImportResultDto importSitesFromExcel(MultipartFile file) {
        int totalRows = 0;
        int successCount = 0;
        int failedCount = 0;

        if (file == null || file.isEmpty()) {
            log.warn("Sites import called with no file or empty file");
            return SiteImportResultDto.builder()
                    .totalRows(totalRows)
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .build();
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                log.warn("Sites import Excel sheet is empty");
                return SiteImportResultDto.builder()
                        .totalRows(totalRows)
                        .successCount(successCount)
                        .failedCount(failedCount)
                        .build();
            }

            // Assume first row is header and skip it
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                // Check if the row is effectively empty
                if (isRowEmpty(row)) {
                    continue;
                }

                totalRows++;

                try {
                    SiteRequestDto dto = readSiteFromRow(row);

                    // Validate mandatory fields (Site name and Site ID)
                    if (dto.getSiteName() == null || dto.getSiteName().isBlank() ||
                        dto.getSiteId() == null || dto.getSiteId().isBlank()) {
                        failedCount++;
                        continue;
                    }

                    Site site = mapToEntity(new Site(), dto);
                    siteRepository.save(site);
                    successCount++;
                } catch (Exception ex) {
                    log.warn("Failed to import row {} from Sites Excel: {}", rowIndex, ex.getMessage());
                    failedCount++;
                }
            }
        } catch (Exception ex) {
            log.error("Error while importing sites from Excel", ex);
        }
        return SiteImportResultDto.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failedCount(failedCount)
                .build();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int cellNum = 0; cellNum < 11; cellNum++) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK &&
                    (cell.getCellType() != CellType.STRING || !cell.getStringCellValue().trim().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private SiteRequestDto readSiteFromRow(Row row) {
        // Columns mapping (STRICT):
        // 0: Site
        // 1: Site ID
        // 2: Address
        // 3: Email
        // 4: Description
        // 5: Contact Person
        // 6: Contact Number
        // 7: Latitude
        // 8: Longitude
        // 9: City
        // 10: Pincode

        String siteName = formatCellAsString(row, 0);
        String siteId = formatCellAsString(row, 1);
        String address = formatCellAsString(row, 2);
        String email = formatCellAsString(row, 3);
        String description = formatCellAsString(row, 4);
        String contactPerson = formatCellAsString(row, 5);
        String contactNumber = formatCellAsString(row, 6); // keep as String even if numeric
        Double latitude = formatCellAsDouble(row, 7);
        Double longitude = formatCellAsDouble(row, 8);
        String city = formatCellAsString(row, 9);
        String pincode = formatCellAsString(row, 10); // keep as String even if numeric

        return SiteRequestDto.builder()
                .siteName(siteName)
                .siteId(siteId)
                .address(address)
                .email(email)
                .description(description)
                .contactPerson(contactPerson)
                .contactNumber(contactNumber)
                .latitude(latitude)
                .longitude(longitude)
                .city(city)
                .pincode(pincode)
                .build();
    }

    private String formatCellAsString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        String value = dataFormatter.formatCellValue(cell);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private Double formatCellAsDouble(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        // First try to read as numeric (NUMERIC or numeric FORMULA)
        try {
            if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                return cell.getNumericCellValue();
            }
        } catch (IllegalStateException ignored) {
            // Fall back to parsing formatted text
        }

        String text = dataFormatter.formatCellValue(cell);
        if (text == null) {
            return null;
        }
        text = text.trim();
        if (text.isEmpty()) {
            return null;
        }

        try {
            text = text.replace(',', '.');
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            // Invalid numeric content; treat as null so the row can still be imported
            return null;
        }
    }

    private Site mapToEntity(Site site, SiteRequestDto dto) {
        site.setSiteName(dto.getSiteName());
        site.setSiteId(dto.getSiteId());
        site.setAddress(dto.getAddress());
        site.setEmail(dto.getEmail());
        site.setDescription(dto.getDescription());
        site.setContactPerson(dto.getContactPerson());
        site.setContactNumber(dto.getContactNumber());
        site.setLatitude(dto.getLatitude());
        site.setLongitude(dto.getLongitude());
        site.setCity(dto.getCity());
        site.setPincode(dto.getPincode());
        return site;
    }

    private SiteResponseDto mapToResponseDto(Site site) {
        return SiteResponseDto.builder()
                .id(site.getId())
                .siteName(site.getSiteName())
                .siteId(site.getSiteId())
                .address(site.getAddress())
                .email(site.getEmail())
                .description(site.getDescription())
                .contactPerson(site.getContactPerson())
                .contactNumber(site.getContactNumber())
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())
                .city(site.getCity())
                .pincode(site.getPincode())
                .build();
    }
}
