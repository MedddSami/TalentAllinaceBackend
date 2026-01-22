package com.jobrecuruitmentplatform.controller;

import com.jobrecuruitmentplatform.exception.BadRequestException;
import com.jobrecuruitmentplatform.exception.ForbiddenException;
import com.jobrecuruitmentplatform.exception.NotFoundException;
import com.jobrecuruitmentplatform.model.JobApplication;
import com.jobrecuruitmentplatform.repository.JobApplicationRepository;
import com.jobrecuruitmentplatform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final JobApplicationRepository applicationRepository;

    @Transactional(readOnly= true)
    @GetMapping("/download/{applicationId}/resume")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long applicationId,
            Authentication authentication) {

        JobApplication application = applicationRepository
                .findByIdWithEmployer(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));


        String userEmail = authentication.getName();

        boolean isEmployer =
                application.getJobOffer()
                        .getCompany()
                        .getEmployer()
                        .getEmail()
                        .equalsIgnoreCase(userEmail);

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isEmployer && !isAdmin) {
            throw new ForbiddenException("You don't have permission to download this file");
        }

        if (application.getResumeFilePath() == null) {
            throw new NotFoundException("Resume not found");
        }

        return downloadFile(
                application.getResumeFilePath(),
                application.getResumeFileName()
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/download/{applicationId}/recommendation")
    public ResponseEntity<Resource> downloadRecommendationLetter(
            @PathVariable Long applicationId,
            Authentication authentication) {

        JobApplication application = applicationRepository
                .findByIdWithEmployer(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));


        String userEmail = authentication.getName();

        boolean isEmployer =
                application.getJobOffer()
                        .getCompany()
                        .getEmployer()
                        .getEmail()
                        .equalsIgnoreCase(userEmail);

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isEmployer && !isAdmin) {
            throw new ForbiddenException("You don't have permission to download this file");
        }

        if (application.getRecommendationLetterFilePath() == null) {
            throw new NotFoundException("Recommendation letter not found");
        }

        return downloadFile(
                application.getRecommendationLetterFilePath(),
                application.getRecommendationLetterFileName()
        );
    }



    private ResponseEntity<Resource> downloadFile(String filePath, String fileName) {
        try {
            Path path = fileStorageService.getFilePath(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new NotFoundException("File not found");
            }

            String contentType = "application/octet-stream";
            if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.endsWith(".doc")) {
                contentType = "application/msword";
            } else if (fileName.endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new BadRequestException("Error downloading file: " + e.getMessage());
        }
    }
}