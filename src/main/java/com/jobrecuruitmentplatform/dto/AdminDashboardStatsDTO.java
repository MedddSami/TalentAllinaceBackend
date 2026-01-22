package com.jobrecuruitmentplatform.dto;

import lombok.Data;

@Data
public class AdminDashboardStatsDTO {

    private long totalCompanies;
    private long companiesThisMonth;

    private long totalJobOffers;
    private long jobOffersThisMonth;

    private long totalApplications;
    private long applicationsLast7Days;

    private long acceptedApplications;
    private double acceptanceRate; // %
}

