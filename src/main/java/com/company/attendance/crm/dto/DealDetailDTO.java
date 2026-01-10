package com.company.attendance.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DealDetailDTO {
    public UUID id;
    public String name;
    public BigDecimal valueAmount;
    public LocalDate closingDate;
    public String stage;
    public OwnerDTO owner;
    public List<ProductDTO> products;
    public List<FileDTO> files;
    public int notesCount;
    public int activitiesCount;

    public static class OwnerDTO { public UUID id; public String name; }
    public static class ProductDTO { public String productName; public BigDecimal listPrice; public BigDecimal quantity; public BigDecimal total; }
    public static class FileDTO { public UUID id; public String fileName; public String uploadedBy; public String createdAt; }
}
