package com.jobrecuruitmentplatform.controller;

import com.jobrecuruitmentplatform.dto.EmployerDashboardStatsDTO;
import com.jobrecuruitmentplatform.model.User;
import com.jobrecuruitmentplatform.repository.UserRepository;
import com.jobrecuruitmentplatform.service.EmployerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/employer/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerDashboardController {

    private final EmployerDashboardService employerDashboardService;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<EmployerDashboardStatsDTO> getStats(
            Authentication authentication
    ) {
        String employerEmail = authentication.getName();
        Optional<User> employerId = userRepository.findByEmail(employerEmail);

        return ResponseEntity.ok(
                employerDashboardService.getStats(employerId.get().getId())
        );
    }
}

