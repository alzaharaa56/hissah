package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.CompanyDocumentRequestDTO;
import com.hissah.DTO.Response.CompanyDocumentResponseDTO;
import com.hissah.Entities.Company;
import com.hissah.Entities.CompanyDocument;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.Role;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.FileStorageException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.CompanyDocumentRepository;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Services.CompanyDocumentService;
import com.hissah.Utilities.FileNameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyDocumentServiceImpl implements CompanyDocumentService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    private final CompanyDocumentRepository companyDocumentRepository;
    private final CompanyRepository companyRepository;

    @Qualifier("uploadRootPath")
    private final Path uploadRootPath;

    @Override
    @Transactional
    public CompanyDocumentResponseDTO upload(
            Long companyId,
            CompanyDocumentRequestDTO request
    ) {
        Company company = getCompany(companyId);

        DocumentType documentType = request.getDocumentType();
        String documentNumber = request.getDocumentNumber();
        LocalDate expiryDate = request.getExpiryDate();
        MultipartFile file = request.getFile();

        if (documentType == null) {
            throw new BusinessRuleException("Document type is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("A document file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("Company documents must not exceed 5 MB.");
        }
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("An expired document cannot be uploaded.");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename()
        );
        String storedName = FileNameGenerator.generate(originalName);

        Path companyDirectory = uploadRootPath
                .resolve("company-documents")
                .resolve(String.valueOf(companyId))
                .normalize();

        if (!companyDirectory.startsWith(uploadRootPath.normalize())) {
            throw new FileStorageException("Invalid company document directory.");
        }

        Path target = FileNameGenerator.resolveSafePath(companyDirectory, storedName);

        try {
            Files.createDirectories(companyDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            deactivatePreviousDocumentType(companyId, documentType);

            CompanyDocument document = new CompanyDocument();
            document.setDocumentType(documentType);
            document.setDocumentNumber(documentNumber);
            document.setFilePath(
                    uploadRootPath.relativize(target).toString().replace("\\", "/")
            );
            document.setExpiryDate(expiryDate);
            document.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
            document.setCompanyId(companyId);

            CompanyDocument saved = companyDocumentRepository.save(document);

            if (company.getVerificationStatus() == VerificationStatus.VERIFIED) {
                company.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
                companyRepository.save(company);
            }

            return toResponse(saved);
        } catch (IOException exception) {
            deleteQuietly(target);
            throw new FileStorageException("Failed to store the company document.", exception);
        } catch (RuntimeException exception) {
            deleteQuietly(target);
            throw exception;
        }
    }

    @Override
    public List<CompanyDocumentResponseDTO> getForCompany(
            Long companyId,
            Long currentCompanyId,
            Role currentRole
    ) {
        validateVisibility(companyId, currentCompanyId, currentRole);

        return companyDocumentRepository.findAll()
                .stream()
                .filter(document ->
                        document.getCompanyId() != null
                                && companyId.equals(document.getCompanyId())
                )
                .sorted(Comparator.comparing(CompanyDocument::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DownloadFile download(
            Long documentId,
            Long currentCompanyId,
            Role currentRole
    ) {
        CompanyDocument document = getDocument(documentId);

        validateVisibility(document.getCompanyId(), currentCompanyId, currentRole);

        Path storedPath = uploadRootPath.resolve(document.getFilePath()).normalize();

        if (!storedPath.startsWith(uploadRootPath.normalize())) {
            throw new FileStorageException("Invalid company document path.");
        }
        if (!Files.isRegularFile(storedPath)) {
            throw new ResourceNotFoundException("The stored company document file is missing.");
        }

        try {
            String contentType = Files.probeContentType(storedPath);
            long fileSize = Files.size(storedPath);

            // استخراج اسم الملف الأصلي من المسار أو استخدام قيمة افتراضية
            String fileName = "document-" + document.getId();

            return new DownloadFile(
                    storedPath,
                    fileName,
                    contentType == null ? "application/octet-stream" : contentType,
                    fileSize
            );
        } catch (IOException exception) {
            throw new FileStorageException("Unable to read the company document.", exception);
        }
    }

    @Override
    @Transactional
    public void delete(Long documentId, Long currentCompanyId) {
        CompanyDocument document = getDocument(documentId);

        if (!document.getCompanyId().equals(currentCompanyId)) {
            throw new UnauthorizedOperationException("You cannot remove another company's document.");
        }

        companyDocumentRepository.delete(document);

        Company company = getCompany(currentCompanyId);
        if (company.getVerificationStatus() == VerificationStatus.VERIFIED) {
            company.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
            companyRepository.save(company);
        }
    }

    private void deactivatePreviousDocumentType(Long companyId, DocumentType documentType) {
        List<CompanyDocument> previous = companyDocumentRepository.findAll()
                .stream()
                .filter(document ->
                        document.getCompanyId() != null
                                && companyId.equals(document.getCompanyId())
                                && document.getDocumentType() == documentType
                )
                .toList();

        companyDocumentRepository.deleteAll(previous);
    }

    private void validateVisibility(Long companyId, Long currentCompanyId, Role currentRole) {
        boolean admin = currentRole == Role.ADMIN;
        boolean owner = companyId.equals(currentCompanyId);

        if (!admin && !owner) {
            throw new UnauthorizedOperationException("You cannot access another company's documents.");
        }
    }

    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
    }

    private CompanyDocument getDocument(Long documentId) {
        return companyDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Company document not found with id: " + documentId));
    }

    private CompanyDocumentResponseDTO toResponse(CompanyDocument document) {
        CompanyDocumentResponseDTO dto = new CompanyDocumentResponseDTO();
        dto.setId(document.getId());
        dto.setDocumentType(document.getDocumentType());
        dto.setDocumentNumber(document.getDocumentNumber());
        dto.setExpiryDate(document.getExpiryDate());
        dto.setVerificationStatus(document.getVerificationStatus());
        dto.setCompanyId(document.getCompanyId());
        return dto;
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}