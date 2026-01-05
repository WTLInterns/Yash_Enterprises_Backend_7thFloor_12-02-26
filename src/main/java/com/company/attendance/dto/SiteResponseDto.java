package com.company.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SiteResponseDto {

    private Long id;

    private String siteName;

    private String siteId;

    private String address;

    private String email;

    private String description;

    private String contactPerson;

    private String contactNumber;

    private Double latitude;

    private Double longitude;

    private String city;

    private String pincode;
}
