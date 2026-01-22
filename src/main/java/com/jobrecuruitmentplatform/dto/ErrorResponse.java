package com.jobrecuruitmentplatform.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> errors;
}
