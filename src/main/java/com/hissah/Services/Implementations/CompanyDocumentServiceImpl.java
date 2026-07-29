package com.hissah.Services.Implementations;

import com.hissah.Entities.CompanyDocument;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CompanyDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyDocumentServiceImpl {

    private final CompanyDocumentRepository companyDocumentRepository;


    private final String uploadDir = "uploads/company-documents/";

    @Transactional
    public CompanyDocument uploadDocument(Long companyId, MultipartFile file, DocumentType documentType, String documentNumber, LocalDate expiryDate) {
        validateDocument(file);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            CompanyDocument document = new CompanyDocument();
            document.setCompanyId(companyId);
            document.setDocumentType(documentType);
            document.setDocumentNumber(documentNumber);
            document.setFilePath(filePath.toString());
            document.setExpiryDate(expiryDate);
            document.setVerificationStatus(VerificationStatus.PENDING_REVIEW);

            return companyDocumentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    public void validateDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Invalid file type. Only PDF and image files are allowed.");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 5MB.");
        }
    }

    @Transactional
    public CompanyDocument updateVerificationStatus(Long documentId, VerificationStatus status) {
        CompanyDocument document = getDocumentById(documentId);
        document.setVerificationStatus(status);
        return companyDocumentRepository.save(document);
    }


    public List<CompanyDocument> getDocumentsByCompanyId(Long companyId) {
        List<CompanyDocument> documents = companyDocumentRepository.findDocumentsByCompanyId(companyId);
        if (documents.isEmpty()) {
            throw new ResourceNotFoundException("No documents found for company id: " + companyId);
        }
        return documents;
    }


    public CompanyDocument getDocumentById(Long documentId) {
        return companyDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Company document not found with id: " + documentId));
    }
}