package com.jobrecuruitmentplatform.service;

import com.jobrecuruitmentplatform.dto.EmployerDashboardStatsDTO;
import com.jobrecuruitmentplatform.repository.JobApplicationRepository;
import com.jobrecuruitmentplatform.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployerDashboardService {

    private final JobOfferRepository jobOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public EmployerDashboardStatsDTO getStats(Long employerId) {

        EmployerDashboardStatsDTO dto = new EmployerDashboardStatsDTO();

        LocalDateTime startOfMonth =
                LocalDate.now().withDayOfMonth(1).atStartOfDay();

        LocalDateTime last7Days =
                LocalDateTime.now().minusDays(7);

        dto.setTotalJobOffers(
                jobOfferRepository.countJobOffersByEmployerId(employerId)
        );

        dto.setJobOffersThisMonth(
                jobOfferRepository.countJobOffersThisMonthByEmployerId(
                        employerId, startOfMonth
                )
        );

        dto.setActiveJobOffers(
                jobOfferRepository.countActiveJobOffersByEmployerId(employerId)
        );

        dto.setTotalApplications(
                jobApplicationRepository.countApplicationsByEmployerId(employerId)
        );

        long newApps =
                jobApplicationRepository.countApplicationsLast7DaysByEmployerId(
                        employerId, last7Days
                );

        dto.setApplicationsLast7Days(newApps);
        dto.setNewApplicationsLast7Days(newApps);

        return dto;
    }
}

