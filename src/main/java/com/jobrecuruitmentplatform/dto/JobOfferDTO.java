package com.jobrecuruitmentplatform.dto;

import com.jobrecuruitmentplatform.model.JobType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class JobOfferDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private JobType jobType;
    private String salaryRange;
    private Long companyId;
    private int applications;
    private String companyName;
    private Boolean active;
    private LocalDateTime createdAt;
}
