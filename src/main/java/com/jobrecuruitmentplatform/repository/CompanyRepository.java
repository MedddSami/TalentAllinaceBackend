package com.jobrecuruitmentplatform.repository;

import com.jobrecuruitmentplatform.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query("""
    SELECT c FROM Company c
    JOIN FETCH c.employer
    WHERE c.employer.id = :employerId
""")
    List<Company> findByEmployerId(@Param("employerId") Long employerId);

    @Query("SELECT COUNT(c) FROM Company c")
    long countCompanies();


    @Query("""
    SELECT COUNT(c)
    FROM Company c
    WHERE c.createdAt >= :startOfMonth
""")
long countCompaniesThisMonth(
    @Param("startOfMonth") LocalDateTime startOfMonth
);
}