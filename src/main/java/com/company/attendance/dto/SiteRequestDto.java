package com.company.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SiteRequestDto {

    /**
     * This field is named `siteName` to match the existing frontend payload.
     */
    @NotBlank(message = "Site name is required")
    @Size(max = 150, message = "Site name must be at most 150 characters")
    private String siteName;

    @NotBlank(message = "Site ID is required")
    @Size(max = 100, message = "Site ID must be at most 100 characters")
    private String siteId;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Email(message = "Email should be valid")
    @Size(max = 200, message = "Email must be at most 200 characters")
    private String email;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @Size(max = 200, message = "Contact person must be at most 200 characters")
    private String contactPerson;

    @Size(max = 50, message = "Contact number must be at most 50 characters")
    private String contactNumber;

    private Double latitude;

    private Double longitude;

    @Size(max = 150, message = "City must be at most 150 characters")
    private String city;

    @Size(max = 20, message = "Pincode must be at most 20 characters")
    private String pincode;
}
