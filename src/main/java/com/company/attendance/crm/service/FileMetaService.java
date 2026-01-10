package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.FileMeta;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.FileMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileMetaService {
    private final DealRepository dealRepository;
    private final FileMetaRepository fileRepo;

    public FileMetaService(DealRepository dealRepository, FileMetaRepository fileRepo) {
        this.dealRepository = dealRepository;
        this.fileRepo = fileRepo;
    }

    public List<FileMeta> list(UUID dealId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        return fileRepo.findByDeal(deal);
    }

    public FileMeta upload(UUID dealId, MultipartFile file, UUID userId) throws IOException {
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        // simple local storage under ./uploads
        Path uploadDir = Path.of("uploads");
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = uploadDir.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        FileMeta meta = new FileMeta();
        meta.setDeal(deal);
        meta.setUploadedBy(userId);
        meta.setFileName(file.getOriginalFilename());
        meta.setFileSize(file.getSize());
        meta.setContentType(file.getContentType());
        meta.setStoragePath(target.toString());
        meta.setCreatedAt(OffsetDateTime.now());
        return fileRepo.save(meta);
    }

    public void delete(UUID fileId){
        fileRepo.findById(fileId).ifPresent(meta -> {
            try { if (meta.getStoragePath() != null) Files.deleteIfExists(Path.of(meta.getStoragePath())); } catch (Exception ignored) {}
            fileRepo.delete(meta);
        });
    }

    public FileMeta get(UUID fileId){
        return fileRepo.findById(fileId).orElseThrow(() -> new IllegalArgumentException("File not found"));
    }
}
