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

@Service
public class FileMetaService {
    private final DealRepository dealRepository;
    private final FileMetaRepository fileRepo;

    public FileMetaService(DealRepository dealRepository, FileMetaRepository fileRepo) {
        this.dealRepository = dealRepository;
        this.fileRepo = fileRepo;
    }

    public List<FileMeta> list(Long dealId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        return fileRepo.findByDeal(deal);
    }

    public FileMeta create(Long dealId, FileMeta meta, Integer userId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        meta.setDeal(deal);
        meta.setCreatedBy(userId);
        return fileRepo.save(meta);
    }

    public FileMeta upload(Long dealId, MultipartFile file, Integer userId) throws IOException {
        Deal deal = dealRepository.findByIdSafe(dealId);
        // simple local storage under ./uploads
        Path uploadDir = Path.of("uploads");
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        String storedName = file.getOriginalFilename();
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

    public void delete(Integer fileId){
        fileRepo.findById(fileId).ifPresent(meta -> {
            try { if (meta.getStoragePath() != null) Files.deleteIfExists(Path.of(meta.getStoragePath())); } catch (Exception ignored) {}
            fileRepo.delete(meta);
        });
    }

    public FileMeta get(Integer fileId){
        return fileRepo.findById(fileId).orElseThrow(() -> new IllegalArgumentException("File not found"));
    }
}
