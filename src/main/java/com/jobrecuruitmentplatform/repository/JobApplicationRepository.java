package com.jobrecuruitmentplatform.repository;

import com.jobrecuruitmentplatform.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCandidateId(Long candidateId);
    List<JobApplication> findByJobOfferId(Long jobOfferId);
    Optional<JobApplication> findByCandidateIdAndJobOfferId(Long candidateId, Long jobOfferId);

    List<JobApplication> findByJobOfferCompanyId(Long companyId);

    // JobApplicationRepository.java
    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.candidate LEFT JOIN FETCH ja.jobOffer")
    List<JobApplication> findAllWithCandidateAndJobOffer();

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.candidate LEFT JOIN FETCH ja.jobOffer")
    Page<JobApplication> findAllWithCandidateAndJobOfferPage(Pageable pageable);

    @Query("""
    SELECT a FROM JobApplication a
    JOIN FETCH a.jobOffer jo
    JOIN FETCH jo.company c
    JOIN FETCH c.employer
    WHERE a.id = :id
""")
    Optional<JobApplication> findByIdWithEmployer(@Param("id") Long id);


    @Query("""
        SELECT ja FROM JobApplication ja
        JOIN FETCH ja.jobOffer jo
        JOIN FETCH jo.company c
        JOIN FETCH c.employer e
        WHERE c.id = :companyId
    """)
    List<JobApplication> findAllByCompanyIdWithJoins(@Param("companyId") Long companyId);

    @Query("""
        SELECT ja FROM JobApplication ja
        JOIN FETCH ja.jobOffer jo
        JOIN FETCH jo.company c
        JOIN FETCH c.employer e
        WHERE c.id = :companyId
    """)
    Page<JobApplication> findAllByCompanyIdWithJoins(@Param("companyId") Long companyId, Pageable pageable);


    @Query("""
    SELECT COUNT(a)
    FROM JobApplication a
    WHERE a.jobOffer.id = :jobOfferId
""")
    int countApplicationsByJobOfferId(@Param("jobOfferId") Long jobOfferId);


    @Query("""
    SELECT COUNT(a)
    FROM JobApplication a
    WHERE a.jobOffer.company.id = :companyId
""")
    int countApplicationsByCompanyId(@Param("companyId") Long companyId);


    @Query("SELECT COUNT(a) FROM JobApplication a")
    long countApplications();

    @Query("""
    SELECT COUNT(a)
    FROM JobApplication a
    WHERE a.status = com.jobrecuruitmentplatform.model.ApplicationStatus.ACCEPTED
""")
    long countAcceptedApplications();

    @Query("""
    SELECT COUNT(a)
    FROM JobApplication a
    WHERE a.appliedAt >= :last7Days
""")
    long countApplicationsLast7Days(
        @Param("last7Days") LocalDateTime last7Days
    );

    @Query("""
    SELECT COUNT(a)
    FROM JobApplication a
    WHERE a.jobOffer.company.employer.id = :employerId
""")
    long countApplicationsByEmployerId(@Param("employerId") Long employerId);

    @Query("""
            SELECT COUNT(a)
            FROM JobApplication a
            WHERE a.jobOffer.company.employer.id = :employerId
            AND a.appliedAt >= :last7Days
""")
    long countApplicationsLast7DaysByEmployerId(
            @Param("employerId") Long employerId,
            @Param("last7Days") LocalDateTime last7Days
    );


}
