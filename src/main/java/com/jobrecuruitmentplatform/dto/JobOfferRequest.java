package com.jobrecuruitmentplatform.dto;

import com.jobrecuruitmentplatform.model.JobType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JobOfferRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotBlank(message = "Salary range is required")
    private String salaryRange;

    @NotNull(message = "Company ID is required")
    private Long companyId;
}
