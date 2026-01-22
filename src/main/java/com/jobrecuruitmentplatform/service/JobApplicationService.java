package com.jobrecuruitmentplatform.service;

import com.jobrecuruitmentplatform.dto.AdminUpdateJobApplicationRequest;
import com.jobrecuruitmentplatform.dto.JobApplicationDTO;
import com.jobrecuruitmentplatform.dto.JobApplicationRequest;
import com.jobrecuruitmentplatform.dto.UpdateApplicationStatusRequest;
import com.jobrecuruitmentplatform.exception.BadRequestException;
import com.jobrecuruitmentplatform.exception.ForbiddenException;
import com.jobrecuruitmentplatform.exception.NotFoundException;
import com.jobrecuruitmentplatform.model.*;
import com.jobrecuruitmentplatform.repository.CompanyRepository;
import com.jobrecuruitmentplatform.repository.JobApplicationRepository;
import com.jobrecuruitmentplatform.repository.JobOfferRepository;
import com.jobrecuruitmentplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CompanyRepository companyRepository;

    public JobApplicationDTO applyToJob(JobApplicationRequest request, String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new NotFoundException("Candidate not found"));

        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new ForbiddenException("Only candidates can apply to jobs");
        }

        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(() -> new NotFoundException("Job offer not found"));

        if (!jobOffer.getActive()) {
            throw new BadRequestException("This job offer is no longer active");
        }

        if (applicationRepository.findByCandidateIdAndJobOfferId(candidate.getId(), jobOffer.getId()).isPresent()) {
            throw new BadRequestException("You have already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .candidate(candidate)
                .jobOffer(jobOffer)
                .coverLetter(request.getCoverLetter())
                .resumeUrl(request.getResumeUrl())
                .status(ApplicationStatus.PENDING)
                .build();

        JobApplication saved = applicationRepository.save(application);
        return mapToDTO(saved);
    }

    public JobApplicationDTO applyToJobWithFiles(JobApplicationRequest request, MultipartFile resume,
                                        MultipartFile recommendationLetter) {
        JobOffer jobOffer = jobOfferRepository.findById(request.getJobOfferId())
                .orElseThrow(() -> new NotFoundException("Job offer not found"));

        if (!jobOffer.getActive()) {
            throw new BadRequestException("This job offer is no longer active");
        }

// Optional: you can check if the same email already applied to the same job
        boolean alreadyApplied = applicationRepository
                .findByJobOfferId(jobOffer.getId())
                .stream()
                .anyMatch(app ->
                        app.getCandidateEmail() != null &&
                                app.getCandidateEmail().equalsIgnoreCase(request.getCandidateEmail())
                );


        if (alreadyApplied) {
            throw new BadRequestException("This candidate has already applied to this job");
        }

// Store resume (required)
        if (resume == null || resume.isEmpty()) {
            throw new BadRequestException("Resume is required");
        }
        String resumePath = fileStorageService.storeFile(resume, "resumes");
        String resumeFileName = resume.getOriginalFilename();

// Store recommendation letter (optional)
        String recommendationPath = null;
        String recommendationFileName = null;
        if (recommendationLetter != null && !recommendationLetter.isEmpty()) {
            recommendationPath = fileStorageService.storeFile(recommendationLetter, "recommendations");
            recommendationFileName = recommendationLetter.getOriginalFilename();
        }

// Build application entity
        JobApplication application = JobApplication.builder()
                .jobOffer(jobOffer)
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .coverLetter(request.getCoverLetter())
                .resumeFileName(resumeFileName)
                .resumeFilePath(resumePath)
                .recommendationLetterFileName(recommendationFileName)
                .recommendationLetterFilePath(recommendationPath)
                .status(ApplicationStatus.PENDING)
                .build();

        JobApplication saved = applicationRepository.save(application);
        return mapToDTO(saved);

    }

    public List<JobApplicationDTO> getMyCandidateApplications(String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new NotFoundException("Candidate not found"));

        return applicationRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsForJobOffer(Long jobOfferId, String employerEmail) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new NotFoundException("Job offer not found"));

        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        if (!jobOffer.getCompany().getEmployer().getId().equals(employer.getId()) && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only view applications for your own job offers");
        }

        return applicationRepository.findByJobOfferId(jobOfferId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsForCompany(Long companyId, String email) throws AccessDeniedException {

        // 🔐 Optional security check (recommended)
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        if (!company.getEmployer().getEmail().equals(email)) {
            throw new AccessDeniedException("You do not own this company");
        }

        return applicationRepository.findAllByCompanyIdWithJoins(companyId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public Page<JobApplicationDTO> getApplicationsForCompanyPaginated( Long companyId, String email, Pageable pageable) throws AccessDeniedException {
        // Check access
        // 🔐 Optional security check (recommended)
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        if (!company.getEmployer().getEmail().equals(email)) {
            throw new AccessDeniedException("You do not own this company");
        }

        return applicationRepository.findAllByCompanyIdWithJoins(companyId, pageable)
                .map(this::mapToDTO);
    }


    public List<JobApplicationDTO> getAllApplications() {
        return applicationRepository.findAllWithCandidateAndJobOffer()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public Page<JobApplicationDTO> getAllApplicationsPaginated(Pageable pageable) {
        return applicationRepository.findAllWithCandidateAndJobOfferPage(pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public JobApplicationDTO adminUpdateApplication(
            Long applicationId,
            AdminUpdateJobApplicationRequest request,
            MultipartFile resume,
            MultipartFile recommendationLetter
    ) {

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        application.setCandidateName(request.getCandidateName());
        application.setCandidateEmail(request.getCandidateEmail());
        application.setCoverLetter(request.getCoverLetter());
        application.setNotes(request.getNotes());
        application.setStatus(request.getStatus());

        if (resume != null && !resume.isEmpty()) {
            application.setResumeFilePath(fileStorageService.storeFile(resume, "resumes"));
            application.setResumeFileName(resume.getOriginalFilename());
        }

        if (recommendationLetter != null && !recommendationLetter.isEmpty()) {
            application.setRecommendationLetterFilePath(
                    fileStorageService.storeFile(recommendationLetter, "recommendations")
            );
            application.setRecommendationLetterFileName(recommendationLetter.getOriginalFilename());
        }

        return mapToDTO(applicationRepository.save(application));
    }

    @Transactional
    public JobApplicationDTO updateApplicationStatus(
            Long applicationId,
            UpdateApplicationStatusRequest request,
            String employerEmail
    ) throws AccessDeniedException {

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        Company company = application.getJobOffer().getCompany();

        if (!company.getEmployer().getEmail().equals(employerEmail)) {
            throw new AccessDeniedException("You do not own this job offer");
        }

        application.setStatus(request.getStatus());
        application.setNotes(request.getNotes());

        return mapToDTO(applicationRepository.save(application));
    }


    public JobApplicationDTO getApplicationById(Long id, String userEmail) {
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isOwner = application.getCandidate().getId().equals(user.getId());
        boolean isEmployer = application.getJobOffer().getCompany().getEmployer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isOwner && !isEmployer && !isAdmin) {
            throw new ForbiddenException("You don't have permission to view this application");
        }

        return mapToDTO(application);
    }

    public void deleteApplication(Long id) {
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        // Delete files
        if (application.getResumeFilePath() != null) {
            fileStorageService.deleteFile(application.getResumeFilePath());
        }
        if (application.getRecommendationLetterFilePath() != null) {
            fileStorageService.deleteFile(application.getRecommendationLetterFilePath());
        }

        applicationRepository.delete(application);
    }

    public void deleteApplication(Long id, String candidateEmail) {
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new NotFoundException("Candidate not found"));

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new ForbiddenException("You can only delete your own applications");
        }

        // Delete files
        if (application.getResumeFilePath() != null) {
            fileStorageService.deleteFile(application.getResumeFilePath());
        }
        if (application.getRecommendationLetterFilePath() != null) {
            fileStorageService.deleteFile(application.getRecommendationLetterFilePath());
        }

        applicationRepository.delete(application);
    }

    private JobApplicationDTO mapToDTO(JobApplication application) {
        JobOffer jobOffer = application.getJobOffer();
        Company company = jobOffer.getCompany();

        Long candidateId = null;
        String candidateName;
        String candidateEmail;

        if (application.getCandidate() != null) {
            candidateId = application.getCandidate().getId();
            candidateName = application.getCandidate().getFirstName(); // or getName()
            candidateEmail = application.getCandidate().getEmail();
        } else {
            candidateName = application.getCandidateName();
            candidateEmail = application.getCandidateEmail();
        }

        return JobApplicationDTO.builder()
                .id(application.getId())
                .candidateId(candidateId)
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .jobOfferId(application.getJobOffer().getId())
                .companyName(company !=null ? company.getName() : null)
                .jobTitle(application.getJobOffer().getTitle())
                .coverLetter(application.getCoverLetter())
                .notes(application.getNotes())
                .resumeFileName(application.getResumeFileName())
                .resumeDownloadUrl(
                        application.getResumeFilePath() != null
                                ? "/api/files/download/" + application.getId() + "/resume"
                                : null
                )
                .recommendationLetterFileName(application.getRecommendationLetterFileName())
                .recommendationLetterDownloadUrl(
                        application.getRecommendationLetterFilePath() != null
                                ? "/api/files/download/" + application.getId() + "/recommendation"
                                : null
                )
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .build();
    }

}
