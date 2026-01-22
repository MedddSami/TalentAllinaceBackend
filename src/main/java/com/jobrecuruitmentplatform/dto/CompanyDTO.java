package com.jobrecuruitmentplatform.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class CompanyDTO {
    private Long id;
    private String name;
    private String description;
    private String website;
    private String location;

    private String email;        // 👈 NEW
    private String size;            // 👈 NEW

    private int jobOffers;       // 👈 COUNT
    private int applications;

    private String phone;
    private String industry;

    private Long employerId;
    private String employerName;
    private String logoUrl;
    private LocalDateTime createdAt;
}
