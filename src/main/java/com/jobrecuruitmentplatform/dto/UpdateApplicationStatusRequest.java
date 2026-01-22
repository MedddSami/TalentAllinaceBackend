package com.jobrecuruitmentplatform.dto;

import com.jobrecuruitmentplatform.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateApplicationStatusRequest {
    @NotNull
    private ApplicationStatus status;

    @Size(max = 2000)
    private String notes;
}

