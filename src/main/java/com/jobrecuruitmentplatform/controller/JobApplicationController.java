package com.jobrecuruitmentplatform.controller;

import com.jobrecuruitmentplatform.dto.AdminUpdateJobApplicationRequest;
import com.jobrecuruitmentplatform.dto.JobApplicationDTO;
import com.jobrecuruitmentplatform.dto.JobApplicationRequest;
import com.jobrecuruitmentplatform.dto.UpdateApplicationStatusRequest;
import com.jobrecuruitmentplatform.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService applicationService;

    @PostMapping
    public ResponseEntity<JobApplicationDTO> applyToJob(
            @Valid @RequestBody JobApplicationRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.applyToJob(request, email));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobApplicationDTO> applyToJobWithFiles(
            @RequestParam("jobOfferId") Long jobOfferId,
            @RequestParam("candidateName") String candidateName,
            @RequestParam("candidateEmail") String candidateEmail,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam("resume") MultipartFile resume,
            @RequestParam(value = "recommendationLetter", required = false) MultipartFile recommendationLetter
    ) {

        JobApplicationRequest request = new JobApplicationRequest();
        request.setJobOfferId(jobOfferId);
        request.setCandidateName(candidateName);
        request.setCandidateEmail(candidateEmail);
        request.setCoverLetter(coverLetter);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.applyToJobWithFiles(
                        request,
                        resume,
                        recommendationLetter
                ));
    }

    @PutMapping("/admin/{applicationId}")
    public ResponseEntity<JobApplicationDTO> adminUpdateApplication(
            @PathVariable Long applicationId,
            @ModelAttribute AdminUpdateJobApplicationRequest request,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestPart(value = "recommendationLetter", required = false) MultipartFile recommendationLetter
    ) {
        return ResponseEntity.ok(
                applicationService.adminUpdateApplication(applicationId, request, resume, recommendationLetter)
        );
    }


    @PatchMapping("/employer/{applicationId}/status")
    public ResponseEntity<JobApplicationDTO> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateApplicationStatusRequest request,
            Authentication authentication
    ) throws AccessDeniedException {

        return ResponseEntity.ok(
                applicationService.updateApplicationStatus(
                        applicationId,
                        request,
                        authentication.getName()
                )
        );
    }



    @Transactional(readOnly = true)
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobApplicationDTO>> getApplicationsForCompany(
            @PathVariable Long companyId,
            Authentication authentication) throws AccessDeniedException {

        return ResponseEntity.ok(
                applicationService.getApplicationsForCompany(companyId, authentication.getName())
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/company/{companyId}/pageable")
    public ResponseEntity<Page<JobApplicationDTO>> getApplicationsForCompanyPaginated(
            @PathVariable Long companyId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy
    ) throws AccessDeniedException {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<JobApplicationDTO> applications = applicationService.getApplicationsForCompanyPaginated(companyId, authentication.getName(), pageable);
        return ResponseEntity.ok(applications);
    }


    // 🔥 Admin / future use
    @GetMapping("/all")
    public ResponseEntity<List<JobApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/all-paginated")
    public ResponseEntity<Page<JobApplicationDTO>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<JobApplicationDTO> applications = applicationService.getAllApplicationsPaginated(pageable);
        return ResponseEntity.ok(applications);
    }


    @GetMapping("/my-applications")
    public ResponseEntity<List<JobApplicationDTO>> getMyCandidateApplications(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.getMyCandidateApplications(email));
    }

    @GetMapping("/job-offer/{jobOfferId}")
    public ResponseEntity<List<JobApplicationDTO>> getApplicationsForJobOffer(
            @PathVariable Long jobOfferId,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.getApplicationsForJobOffer(jobOfferId, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationDTO> getApplicationById(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.getApplicationById(id, email));
    }

    @DeleteMapping("/admin/{applicationId}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long applicationId) {
        applicationService.deleteApplication(applicationId);
        return ResponseEntity.noContent().build();
    }

}
