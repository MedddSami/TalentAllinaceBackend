package com.jobrecuruitmentplatform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CompanyRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Website is required")
    private String website;

    private String location;

    private String email;        // 👈 NEW
    private String size;            // 👈 NEW

    private int jobOffers;       // 👈 COUNT
    private int applications;

    private String phone;
    private String industry;

    private String logoUrl;
}
