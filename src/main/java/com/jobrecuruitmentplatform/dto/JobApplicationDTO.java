package com.jobrecuruitmentplatform.dto;

import com.jobrecuruitmentplatform.model.ApplicationStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class JobApplicationDTO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long jobOfferId;
    private String jobTitle;
    private String coverLetter;
    private String notes;
    private String companyName;
    private String resumeUrl;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private String resumeFileName;
    private String resumeDownloadUrl;
    private String recommendationLetterFileName;
    private String recommendationLetterDownloadUrl;
}
