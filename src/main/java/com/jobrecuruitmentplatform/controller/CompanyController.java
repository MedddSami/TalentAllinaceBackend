package com.jobrecuruitmentplatform.controller;

import com.jobrecuruitmentplatform.dto.CompanyDTO;
import com.jobrecuruitmentplatform.dto.CompanyRequest;
import com.jobrecuruitmentplatform.model.Company;
import com.jobrecuruitmentplatform.service.CompanyService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Transactional
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(
            @Valid @RequestBody CompanyRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.createCompany(request, email));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CompanyDTO>> getAllCompaniesForAdmin() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/admin/all-pageable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CompanyDTO>> getAllCompaniesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<CompanyDTO> companies = companyService.getAllCompaniesPageable(pageable);
        return ResponseEntity.ok(companies);
    }


    @Transactional(readOnly = true)
    @GetMapping("/my-companies")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<CompanyDTO>> getMyCompanies(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return ResponseEntity.ok(companyService.getMyCompanies(authentication.getName()));
    }

    @PostMapping("/{id}/logo")
    public ResponseEntity<CompanyDTO> uploadLogo(
            @PathVariable Long id,
            @RequestParam("logo") MultipartFile logo,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(companyService.uploadLogo(id, logo, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(companyService.updateCompany(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        companyService.deleteCompany(id, email);
        return ResponseEntity.noContent().build();
    }
}
