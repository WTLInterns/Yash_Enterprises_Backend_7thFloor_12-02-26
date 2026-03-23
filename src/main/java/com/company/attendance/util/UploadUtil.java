package com.company.attendance.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class UploadUtil {

    @Value("${expenses.upload.dir:C:/uploads/expenses/}")
    private String expensesUploadDir;

    @Value("${invoices.upload.dir:C:/uploads/invoices/}")
    private String invoicesUploadDir;

    public Path ensureExpensesDirectoryExists() throws IOException {
        Path path = Paths.get(expensesUploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    public Path ensureInvoicesDirectoryExists() throws IOException {
        Path path = Paths.get(invoicesUploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    public String saveFile(MultipartFile file, String type) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot save empty file");
        }

        Path uploadPath;
        if ("expenses".equalsIgnoreCase(type)) {
            uploadPath = ensureExpensesDirectoryExists();
        } else if ("invoices".equalsIgnoreCase(type)) {
            uploadPath = ensureInvoicesDirectoryExists();
        } else {
            throw new IllegalArgumentException("Unknown upload type: " + type);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        
        Path filePath = uploadPath.resolve(fileName);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative URL
        if ("expenses".equalsIgnoreCase(type)) {
            return "/uploads/expenses/" + fileName;
        } else {
            return "/uploads/invoices/" + fileName;
        }
    }

    public String getExpensesUploadDir() {
        return expensesUploadDir;
    }

    public String getInvoicesUploadDir() {
        return invoicesUploadDir;
    }
}
