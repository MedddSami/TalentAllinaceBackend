package com.jobrecuruitmentplatform.dto;

import com.jobrecuruitmentplatform.model.UserRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private LocalDateTime createdAt;
}
