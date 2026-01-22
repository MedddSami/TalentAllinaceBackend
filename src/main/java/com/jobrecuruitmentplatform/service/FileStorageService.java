package com.jobrecuruitmentplatform.service;

import com.jobrecuruitmentplatform.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-file-size}")
    private long maxFileSize;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);

            // Create subdirectories
            Files.createDirectories(this.fileStorageLocation.resolve("resumes"));
            Files.createDirectories(this.fileStorageLocation.resolve("recommendations"));
            Files.createDirectories(this.fileStorageLocation.resolve("logos"));

            log.info("File storage initialized at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum limit of " + (maxFileSize / 1048576) + "MB");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Validate file extension
        String extension = getFileExtension(originalFileName);
        if (!isValidFileExtension(extension, subDirectory)) {
            throw new BadRequestException("Invalid file type. Allowed types: " + getAllowedExtensions(subDirectory));
        }

        try {
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Invalid file path");
            }

            String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
            Path targetLocation = this.fileStorageLocation.resolve(subDirectory).resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", fileName);
            return subDirectory + "/" + fileName;

        } catch (IOException ex) {
            log.error("Error storing file: {}", ex.getMessage());
            throw new BadRequestException("Could not store file. Please try again!");
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            Path path = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(path);
            log.info("File deleted successfully: {}", filePath);
        } catch (IOException ex) {
            log.error("Error deleting file: {}", ex.getMessage());
        }
    }

    public Path getFilePath(String filePath) {
        return this.fileStorageLocation.resolve(filePath).normalize();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isValidFileExtension(String extension, String subDirectory) {
        return switch (subDirectory) {
            case "resumes", "recommendations" ->
                    extension.matches("pdf|doc|docx");
            case "logos" ->
                    extension.matches("jpg|jpeg|png|gif|svg");
            default -> false;
        };
    }

    private String getAllowedExtensions(String subDirectory) {
        return switch (subDirectory) {
            case "resumes", "recommendations" -> "PDF, DOC, DOCX";
            case "logos" -> "JPG, JPEG, PNG, GIF, SVG";
            default -> "";
        };
    }
}
