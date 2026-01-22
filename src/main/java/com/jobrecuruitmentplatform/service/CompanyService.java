package com.jobrecuruitmentplatform.service;

import com.jobrecuruitmentplatform.dto.CompanyDTO;
import com.jobrecuruitmentplatform.dto.CompanyRequest;
import com.jobrecuruitmentplatform.exception.ForbiddenException;
import com.jobrecuruitmentplatform.exception.NotFoundException;
import com.jobrecuruitmentplatform.model.Company;
import com.jobrecuruitmentplatform.model.User;
import com.jobrecuruitmentplatform.model.UserRole;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final JobOfferRepository jobOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public CompanyDTO createCompany(CompanyRequest request, String employerEmail) {
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        if (employer.getRole() != UserRole.EMPLOYER && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only employers can create companies");
        }

        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .location(request.getLocation())
                .employer(employer)
                .build();

        Company saved = companyRepository.save(company);
        return mapToDTO(saved);
    }

    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public Page<CompanyDTO> getAllCompaniesPageable(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(this::mapToDTO);
    }


    public List<CompanyDTO> getMyCompanies(String employerEmail) {
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        return companyRepository.findByEmployerId(employer.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CompanyDTO uploadLogo(Long id, MultipartFile logoFile, String employerEmail) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        if (!company.getEmployer().getId().equals(employer.getId()) && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only update your own companies");
        }

        // Delete old logo if exists
        if (company.getLogoUrl() != null) {
            fileStorageService.deleteFile(company.getLogoUrl());
        }

        // Store new logo
        String logoPath = fileStorageService.storeFile(logoFile, "logos");
        company.setLogoUrl(logoPath);

        Company updated = companyRepository.save(company);
        return mapToDTO(updated);
    }

    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        return mapToDTO(company);
    }

    public CompanyDTO updateCompany(Long id, CompanyRequest request, String employerEmail) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        if (!company.getEmployer().getId().equals(employer.getId()) && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only update your own companies");
        }

        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setPhone(request.getPhone());
        company.setSize(request.getSize());
        company.setIndustry(request.getIndustry());
        company.setLogoUrl(request.getLogoUrl());

        Company updated = companyRepository.save(company);
        return mapToDTO(updated);
    }

    public void deleteCompany(Long id, String employerEmail) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        if (!company.getEmployer().getId().equals(employer.getId()) && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only delete your own companies");
        }

        companyRepository.delete(company);
    }

    private CompanyDTO mapToDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .location(company.getLocation())
                .logoUrl(company.getLogoUrl())
                .phone(company.getPhone())
                .size(company.getSize())
                .industry(company.getIndustry())
                .email(company.getEmployer().getEmail())
        // ✅ COUNTS
                .jobOffers(
                    jobOfferRepository.countJobOffersByCompanyId(company.getId())
                )
                .applications(
                    jobApplicationRepository.countApplicationsByCompanyId(company.getId())
                )
                //.employerId(company.getEmployer().getId())
                //.employerName(company.getEmployer().getFirstName() + " " + company.getEmployer().getLastName())
                .createdAt(company.getCreatedAt())
                .build();
    }
}