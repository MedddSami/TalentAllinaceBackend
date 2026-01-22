package com.jobrecuruitmentplatform.dto;

import com.jobrecuruitmentplatform.model.ApplicationStatus;
import lombok.Data;

@Data
public class AdminUpdateJobApplicationRequest {

    private String candidateName;
    private String candidateEmail;
    private String coverLetter;
    private String notes;
    private ApplicationStatus status;
}

