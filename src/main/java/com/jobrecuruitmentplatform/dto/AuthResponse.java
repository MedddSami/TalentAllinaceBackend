package com.jobrecuruitmentplatform.dto;

import lombok.*;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UserDTO user;
}
