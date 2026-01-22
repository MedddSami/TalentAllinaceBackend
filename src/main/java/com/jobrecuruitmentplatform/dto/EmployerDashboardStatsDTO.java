package com.jobrecuruitmentplatform.dto;

import lombok.Data;

@Data
public class EmployerDashboardStatsDTO {

    private long totalJobOffers;
    private long jobOffersThisMonth;

    private long activeJobOffers;

    private long totalApplications;
    private long applicationsLast7Days;

    private long newApplicationsLast7Days;
}

