package com.company.attendance.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DealDetailDTO {
    public Long id;
    public String name;
    public BigDecimal valueAmount;
    public LocalDate closingDate;
    public String stage;
    public OwnerDTO owner;
    public List<ProductDTO> products;
    public List<FileDTO> files;
    public int notesCount;
    public int activitiesCount;

    public static class OwnerDTO { public Long id; public String name; }
    public static class ProductDTO { public String productName; public BigDecimal listPrice; public BigDecimal quantity; public BigDecimal total; }
    public static class FileDTO { public Long id; public String fileName; public String uploadedBy; public String createdAt; }
}
