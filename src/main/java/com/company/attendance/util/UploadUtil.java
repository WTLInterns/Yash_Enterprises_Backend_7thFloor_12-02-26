package com.company.attendance.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public String getExpensesUploadDir() {
        return expensesUploadDir;
    }

    public String getInvoicesUploadDir() {
        return invoicesUploadDir;
    }
}
