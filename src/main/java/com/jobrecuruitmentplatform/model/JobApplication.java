package com.jobrecuruitmentplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "candidate_id", nullable = true)
    private User candidate;


    private String candidateName;

    private String candidateEmail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    // 🔹 NEW
    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String coverLetter;

    private String resumeUrl;

    private String resumeFileName;

    private String resumeFilePath;

    private String recommendationLetterFileName;

    private String recommendationLetterFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
