package com.jobrecuruitmentplatform.repository;

import com.jobrecuruitmentplatform.model.JobOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    List<JobOffer> findByActiveTrue();


    @Query("""
  SELECT jo
  FROM JobOffer jo
  JOIN FETCH jo.company
  WHERE jo.company.id = :companyId
""")
    List<JobOffer> findByCompanyId(@Param("companyId") Long companyId);

    @Query("""
  SELECT jo
  FROM JobOffer jo
  JOIN FETCH jo.company
  WHERE jo.company.id = :companyId
""")
    Page<JobOffer> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);


    @Query("""
    SELECT COUNT(j)
    FROM JobOffer j
    WHERE j.company.id = :companyId
""")
    int countJobOffersByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(j) FROM JobOffer j")
    long countJobOffers();


    @Query("""
    SELECT COUNT(j)
    FROM JobOffer j
    WHERE j.createdAt >= :startOfMonth
""")
    long countJobOffersThisMonth(
        @Param("startOfMonth") LocalDateTime startOfMonth
    );


    @Query("""
    SELECT COUNT(j)
    FROM JobOffer j
    WHERE j.company.employer.id = :employerId
""")
    long countJobOffersByEmployerId(@Param("employerId") Long employerId);

@Query("""
            SELECT COUNT(j)
            FROM JobOffer j
            WHERE j.company.employer.id = :employerId
            AND j.createdAt >= :startOfMonth
""")
    long countJobOffersThisMonthByEmployerId(
            @Param("employerId") Long employerId,
            @Param("startOfMonth") LocalDateTime startOfMonth
    );

    @Query("""
    SELECT COUNT(j)
    FROM JobOffer j
    WHERE j.company.employer.id = :employerId
      AND j.active = true
""")
    long countActiveJobOffersByEmployerId(@Param("employerId") Long employerId);

}
