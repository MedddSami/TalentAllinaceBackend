package com.jobrecuruitmentplatform.service;

import com.jobrecuruitmentplatform.dto.JobOfferDTO;
import com.jobrecuruitmentplatform.dto.JobOfferRequest;
import com.jobrecuruitmentplatform.exception.ForbiddenException;
import com.jobrecuruitmentplatform.exception.NotFoundException;
import com.jobrecuruitmentplatform.model.Company;
import com.jobrecuruitmentplatform.model.JobOffer;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public JobOfferDTO createJobOffer(JobOfferRequest request, String employerEmail) {
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Company not found"));

        if (!company.getEmployer().getId().equals(employer.getId()) && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only create job offers for your own companies");
        }

        JobOffer jobOffer = JobOffer.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .salaryRange(request.getSalaryRange())
                .company(company)
                .active(true)
                .build();

        JobOffer saved = jobOfferRepository.save(jobOffer);
        return mapToDTO(saved);
    }

    public List<JobOfferDTO> getAllActiveJobOffers() {
        return jobOfferRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<JobOfferDTO> getAllActiveJobOffersPaginated(Pageable pageable) {
        return jobOfferRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    public JobOfferDTO getJobOfferById(Long id) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job offer not found"));
        return mapToDTO(jobOffer);
    }

    public List<JobOfferDTO> getJobOffersByCompany(Long companyId) {
        return jobOfferRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<JobOfferDTO> getJobOffersByCompanyPaginated(Long companyId, Pageable pageable) {
        return jobOfferRepository.findByCompanyId(companyId, pageable)
                .map(this::mapToDTO);
    }


    public JobOfferDTO updateJobOffer(Long id, JobOfferRequest request, String employerEmail) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job offer not found"));

        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        if (!jobOffer.getCompany().getEmployer().getId().equals(employer.getId()) && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only update your own job offers");
        }

        jobOffer.setTitle(request.getTitle());
        jobOffer.setDescription(request.getDescription());
        jobOffer.setLocation(request.getLocation());
        jobOffer.setJobType(request.getJobType());
        jobOffer.setSalaryRange(request.getSalaryRange());

        JobOffer updated = jobOfferRepository.save(jobOffer);
        return mapToDTO(updated);
    }

    public void deleteJobOffer(Long id, String employerEmail) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job offer not found"));

        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        if (!jobOffer.getCompany().getEmployer().getId().equals(employer.getId()) && employer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only delete your own job offers");
        }

        jobOfferRepository.delete(jobOffer);
    }

    private JobOfferDTO mapToDTO(JobOffer jobOffer) {
        return JobOfferDTO.builder()
                .id(jobOffer.getId())
                .title(jobOffer.getTitle())
                .description(jobOffer.getDescription())
                .location(jobOffer.getLocation())
                .jobType(jobOffer.getJobType())
                .salaryRange(jobOffer.getSalaryRange())
                .companyId(jobOffer.getCompany().getId())
                .companyName(jobOffer.getCompany().getName())
                .applications(jobApplicationRepository.countApplicationsByJobOfferId(jobOffer.getId()))
                .active(jobOffer.getActive())
                .createdAt(jobOffer.getCreatedAt())
                .build();
    }
}
