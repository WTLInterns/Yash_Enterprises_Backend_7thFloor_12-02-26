package com.company.attendance.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ClientDto {
    private UUID id;
    
    @NotBlank(message = "Client name is required")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String contactPhone;
    private String address;
    private String notes;
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Explicit getters/setters used by services/controllers (defensive if Lombok is not processed)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
