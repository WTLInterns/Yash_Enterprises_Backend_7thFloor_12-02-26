package com.company.attendance.crm.dto;

import com.company.attendance.entity.CustomerAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClientWithDealsDto {

    // ── Client fields ──────────────────────────────────────────
    private Long id;
    private String name;
    private String email;
    private String contactPhone;
    private String contactName;
    private String contactNumber;
    private String ownerName;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Addresses (all types) ──────────────────────────────────
    private List<AddressDto> addresses;

    // ── All deals for this client ──────────────────────────────
    private List<DealSummaryDto> deals;

    @Data
    public static class AddressDto {
        private Long id;
        private String addressType;
        private String addressLine;
        private String city;
        private String state;
        private String pincode;
        private String taluka;
        private String district;
        private Double latitude;
        private Double longitude;
        private Boolean isPrimary;
    }

    @Data
    public static class DealSummaryDto {
        private Long id;
        private String dealCode;
        private String stageCode;
        private String department;
        private Long bankId;
        private String bankName;
        private String branchName;
        private String taluka;
        private String district;
        private BigDecimal valueAmount;
        private BigDecimal calculatedValue;
        private LocalDate closingDate;
        private String description;
        private Boolean movedToApproval;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<String> productNames;
    }
}
