package com.jobrecuruitmentplatform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JobApplicationRequest {

    @NotNull(message = "Job offer ID is required")
    private Long jobOfferId;

    @Size(max = 2000, message = "Cover letter must not exceed 2000 characters")
    private String coverLetter;

    private String recommendationLetter;

    private String resumeUrl;

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    @Email(message = "Candidate email must be valid")
    @NotBlank(message = "Candidate email is required")
    private String candidateEmail;
}

