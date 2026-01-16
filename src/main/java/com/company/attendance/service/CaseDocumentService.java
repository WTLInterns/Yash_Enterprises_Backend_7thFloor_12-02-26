package com.company.attendance.service;

import com.company.attendance.dto.CaseDocumentDto;
import com.company.attendance.entity.Case;
import com.company.attendance.entity.CaseDocument;
import com.company.attendance.mapper.CaseDocumentMapper;
import com.company.attendance.repository.CaseDocumentRepository;
import com.company.attendance.repository.CaseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CaseDocumentService {
    
    private static final Logger log = LoggerFactory.getLogger(CaseDocumentService.class);

    private final CaseDocumentRepository caseDocumentRepository;
    private final CaseRepository caseRepository;
    private final CaseDocumentMapper caseDocumentMapper;

    public CaseDocumentDto uploadDocument(CaseDocumentDto documentDto, org.springframework.web.multipart.MultipartFile file) throws IOException {
        log.info("Uploading document for case ID: {}", documentDto.getCaseId());
        
        // Validate case exists
        Case caseEntity = caseRepository.findById(documentDto.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + documentDto.getCaseId()));
        
        // Create upload directory if it doesn't exist
        String uploadDir = "uploads/documents";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Save file to disk
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + "/" + fileName;
        Files.write(Paths.get(filePath), file.getBytes());
        
        // Create document entity
        CaseDocument document = caseDocumentMapper.toEntity(documentDto);
        document.setFileName(fileName);
        document.setFilePath(filePath);
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setCaseEntity(caseEntity);
        
        CaseDocument savedDocument = caseDocumentRepository.save(document);
        
        log.info("Document uploaded successfully with ID: {}", savedDocument.getId());
        return caseDocumentMapper.toDto(savedDocument);
    }

    public CaseDocumentDto updateDocument(Long id, CaseDocumentDto documentDto) {
        log.info("Updating document with ID: {}", id);
        
        CaseDocument existing = caseDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Update fields
        if (documentDto.getDocumentName() != null) {
            existing.setDocumentName(documentDto.getDocumentName());
        }
        if (documentDto.getDescription() != null) {
            existing.setDescription(documentDto.getDescription());
        }
        
        CaseDocument updated = caseDocumentRepository.save(existing);
        
        log.info("Document updated successfully with ID: {}", updated.getId());
        return caseDocumentMapper.toDto(updated);
    }

    public CaseDocumentDto getDocumentById(Long id) {
        log.info("Fetching document with ID: {}", id);
        
        CaseDocument document = caseDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        return caseDocumentMapper.toDto(document);
    }

    public List<CaseDocumentDto> getDocumentsByCaseId(Long caseId) {
        log.info("Fetching documents for case ID: {}", caseId);

        if (caseId == null || !caseRepository.existsById(caseId)) {
            return List.of();
        }

        List<CaseDocument> documents = caseDocumentRepository.findByCaseEntityId(caseId);
        return documents.stream()
                .filter(doc -> {
                    String filePath = doc.getFilePath();
                    if (filePath == null || filePath.isBlank()) {
                        log.warn("CaseDocument {} has empty filePath; deleting orphan record", doc.getId());
                        if (doc.getId() != null) caseDocumentRepository.deleteById(doc.getId());
                        return false;
                    }
                    Path path = Paths.get(filePath);
                    if (!Files.exists(path)) {
                        log.warn("CaseDocument {} points to missing file {}; deleting orphan record", doc.getId(), filePath);
                        if (doc.getId() != null) caseDocumentRepository.deleteById(doc.getId());
                        return false;
                    }
                    return true;
                })
                .map(caseDocumentMapper::toDto)
                .toList();
    }

    public void deleteDocument(Long id) {
        log.info("Deleting document with ID: {}", id);
        
        CaseDocument document = caseDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Delete file from disk
        try {
            if (document.getFilePath() != null) {
                Files.deleteIfExists(Paths.get(document.getFilePath()));
            }
        } catch (IOException e) {
            log.warn("Could not delete file from disk: {}", e.getMessage());
        }
        
        caseDocumentRepository.deleteById(id);
        
        log.info("Document deleted successfully with ID: {}", id);
    }

    public List<CaseDocumentDto> searchDocuments(String search) {
        log.info("Searching documents with term: {}", search);
        
        // Simple implementation using findAll and filtering
        List<CaseDocument> allDocuments = caseDocumentRepository.findAll();
        return allDocuments.stream()
                .filter(doc -> doc.getDocumentName().toLowerCase().contains(search.toLowerCase()) ||
                           (doc.getDescription() != null && doc.getDescription().toLowerCase().contains(search.toLowerCase())))
                .map(caseDocumentMapper::toDto)
                .toList();
    }

    public Long getDocumentsCountByCaseId(Long caseId) {
        return caseDocumentRepository.countByCaseEntityId(caseId);
    }

    public byte[] downloadDocument(Long id) throws IOException {
        log.info("Downloading document with ID: {}", id);
        
        CaseDocument document = caseDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        return Files.readAllBytes(Paths.get(document.getFilePath()));
    }
}
