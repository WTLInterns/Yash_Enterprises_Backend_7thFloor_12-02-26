package com.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressEditRequestDTO {

    private Long id; // ✅ Fixed: was requestId

    // Employee
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;

    // Customer
    private Long customerId;
    private String customerName;

    // Address
    private Long addressId;
    private String oldAddress;
    private String addressType;

    // New address
    private String newAddressLine;
    private String newCity;
    private String newState;
    private String newPincode;
    private String newCountry;
    private Double newLatitude;
    private Double newLongitude;

    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
