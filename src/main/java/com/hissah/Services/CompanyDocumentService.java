package com.hissah.Services;

import com.hissah.Configuration.FileStorageConfig;
import com.hissah.DTO.Response.CompanyDocumentResponseDTO;
import com.hissah.Entities.CompanyDocument;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CompanyDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyDocumentService {

    private final CompanyDocumentRepository companyDocumentRepository;
    private final FileStorageConfig fileStorageConfig;


    @Transactional
    public CompanyDocumentResponseDTO uploadDocument(Long companyId, MultipartFile file, DocumentType documentType, String documentNumber, LocalDate expiryDate) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".pdf") && !originalFilename.endsWith(".jpg") && !originalFilename.endsWith(".png"))) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, JPG, and PNG files are allowed.");
        }

        try {
            Path uploadDir = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path targetLocation = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);


            CompanyDocument document = new CompanyDocument();
            document.setCompanyId(companyId);
            document.setDocumentType(documentType);
            document.setDocumentNumber(documentNumber);
            document.setFilePath(targetLocation.toString());
            document.setExpiryDate(expiryDate);
            document.setVerificationStatus(VerificationStatus.PENDING);

            CompanyDocument savedDocument = companyDocumentRepository.save(document);

            return CompanyDocumentResponseDTO.fromEntity(savedDocument);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }


    public Resource getDocumentFile(Long documentId) {
        CompanyDocument document = companyDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable on the server.");
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path error: " + ex.getMessage());
        }
    }
}