package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.FileMeta;
import com.company.attendance.crm.service.FileMetaService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
public class FileMetaController {
    private final FileMetaService fileService;

    public FileMetaController(FileMetaService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/api/deals/{dealId}/files")
    public List<FileMeta> list(@PathVariable Long dealId){
        return fileService.list(dealId);
    }

    @PostMapping(value = "/api/deals/{dealId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileMeta upload(@PathVariable Long dealId,
                           @RequestPart("file") MultipartFile file,
                           @RequestHeader(value = "X-User-Id", required = false) Integer userId) throws Exception {
        return fileService.upload(dealId, file, userId);
    }

    @GetMapping("/api/files/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Integer fileId) throws MalformedURLException {
        FileMeta meta = fileService.get(fileId);
        Path path = Path.of(meta.getStoragePath());
        Resource res = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(meta.getContentType() != null ? meta.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(res);
    }

    @DeleteMapping("/api/files/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Integer fileId){
        fileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }
}
