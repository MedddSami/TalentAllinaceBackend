package com.jobrecuruitmentplatform.controller;

import com.jobrecuruitmentplatform.dto.JobOfferDTO;
import com.jobrecuruitmentplatform.dto.JobOfferRequest;
import com.jobrecuruitmentplatform.service.JobOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    @PostMapping
    public ResponseEntity<JobOfferDTO> createJobOffer(
            @Valid @RequestBody JobOfferRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobOfferService.createJobOffer(request, email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/public/all")
    public ResponseEntity<List<JobOfferDTO>> getAllActiveJobOffers() {
        return ResponseEntity.ok(jobOfferService.getAllActiveJobOffers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/public/all/paginated")
    public ResponseEntity<Page<JobOfferDTO>> getAllActiveJobOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<JobOfferDTO> offers = jobOfferService.getAllActiveJobOffersPaginated(pageable);
        return ResponseEntity.ok(offers);
    }


    @GetMapping("/{id}")
    public ResponseEntity<JobOfferDTO> getJobOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(jobOfferService.getJobOfferById(id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobOfferDTO>> getJobOffersByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(jobOfferService.getJobOffersByCompany(companyId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/company/{companyId}/paginated")
    public ResponseEntity<Page<JobOfferDTO>> getJobOffersByCompanyPaginated(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<JobOfferDTO> offers = jobOfferService.getJobOffersByCompanyPaginated(companyId, pageable);
        return ResponseEntity.ok(offers);
    }


    @PutMapping("/{id}")
    public ResponseEntity<JobOfferDTO> updateJobOffer(
            @PathVariable Long id,
            @Valid @RequestBody JobOfferRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobOfferService.updateJobOffer(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        jobOfferService.deleteJobOffer(id, email);
        return ResponseEntity.noContent().build();
    }
}
