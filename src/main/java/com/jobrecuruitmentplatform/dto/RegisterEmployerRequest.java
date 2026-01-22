package com.jobrecuruitmentplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterEmployerRequest {
    // User details
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    // Company details
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Company description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String companyDescription;

    @NotBlank(message = "Company website is required")
    private String companyWebsite;

    @NotBlank(message = "Company location is required")
    private String companyLocation;
}
