package com.company.attendance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {

    private Integer id;
    private String name;
    private String code;
    private String address;
    private Boolean isActive;
    private String contactEmail;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
