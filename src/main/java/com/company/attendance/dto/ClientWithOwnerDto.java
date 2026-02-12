package com.company.attendance.dto;

import com.company.attendance.entity.Client;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientWithOwnerDto {
    private Long id;
    private String name;
    private String email;
    private String contactPhone;
    private String address;
    private String city;
    private String pincode;
    private String state;
    private String country;
    private String contactName;
    private String contactNumber;
    private String countryCode;
    private String notes;
    private Boolean isActive;
    private String customFields;
    private Double latitude;
    private Double longitude;
    private String createdAt;
    private String updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long ownerId;
    
    // Owner information
    private OwnerInfo owner;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String fullName;
        private String firstName;
        private String lastName;
        private String role;
    }
    
    public static ClientWithOwnerDto fromEntity(Client client, OwnerInfo ownerInfo) {
        ClientWithOwnerDto dto = new ClientWithOwnerDto();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setContactPhone(client.getContactPhone());
        dto.setAddress(client.getAddress());
        dto.setCity(client.getCity());
        dto.setPincode(client.getPincode());
        dto.setState(client.getState());
        dto.setCountry(client.getCountry());
        dto.setContactName(client.getContactName());
        dto.setContactNumber(client.getContactNumber());
        dto.setCountryCode(client.getCountryCode());
        dto.setNotes(client.getNotes());
        dto.setIsActive(client.getIsActive());
        dto.setCustomFields(client.getCustomFields());
        dto.setLatitude(client.getLatitude());
        dto.setLongitude(client.getLongitude());
        dto.setCreatedAt(client.getCreatedAt() != null ? client.getCreatedAt().toString() : null);
        dto.setUpdatedAt(client.getUpdatedAt() != null ? client.getUpdatedAt().toString() : null);
        dto.setCreatedBy(client.getCreatedBy());
        dto.setUpdatedBy(client.getUpdatedBy());
        dto.setOwnerId(client.getOwnerId());
        dto.setOwner(ownerInfo);
        return dto;
    }
}
