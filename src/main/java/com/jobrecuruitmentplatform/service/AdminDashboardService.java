package com.jobrecuruitmentplatform.service;

import com.jobrecuruitmentplatform.dto.AdminDashboardStatsDTO;
import com.jobrecuruitmentplatform.repository.CompanyRepository;
import com.jobrecuruitmentplatform.repository.JobApplicationRepository;
import com.jobrecuruitmentplatform.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final CompanyRepository companyRepository;
    private final JobOfferRepository jobOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public AdminDashboardStatsDTO getStats() {
        AdminDashboardStatsDTO dto = new AdminDashboardStatsDTO();

        LocalDateTime startOfMonth =
                LocalDate.now().withDayOfMonth(1).atStartOfDay();

        LocalDateTime last7Days =
                LocalDateTime.now().minusDays(7);

        long totalApplications = jobApplicationRepository.countApplications();
        long acceptedApplications =
                jobApplicationRepository.countAcceptedApplications();

        dto.setTotalCompanies(companyRepository.countCompanies());
        dto.setCompaniesThisMonth(
                companyRepository.countCompaniesThisMonth(startOfMonth)
        );

        dto.setTotalJobOffers(jobOfferRepository.countJobOffers());
        dto.setJobOffersThisMonth(
                jobOfferRepository.countJobOffersThisMonth(startOfMonth)
        );

        dto.setTotalApplications(totalApplications);
        dto.setApplicationsLast7Days(
                jobApplicationRepository.countApplicationsLast7Days(last7Days)
        );

        dto.setAcceptedApplications(acceptedApplications);

        dto.setAcceptanceRate(
                totalApplications == 0
                        ? 0
                        : (acceptedApplications * 100.0) / totalApplications
        );

        return dto;
    }
}