package com.hissah.DTO.Request;

import com.hissah.Entities.CompanyDocument;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.VerificationStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class CompanyDocumentRequestDTO {
    private DocumentType documentType;
    private String documentNumber;
    private LocalDate expiryDate;
    private VerificationStatus verificationStatus;
    private Long companyId;
    private MultipartFile file;

    public CompanyDocument toEntity(String filePath) {
        CompanyDocument document = new CompanyDocument();
        document.setDocumentType(this.documentType);
        document.setDocumentNumber(this.documentNumber);
        document.setExpiryDate(this.expiryDate);
        document.setVerificationStatus(this.verificationStatus != null ? this.verificationStatus : VerificationStatus.PENDING_REVIEW);
        document.setCompanyId(this.companyId);
        document.setFilePath(filePath);
        return document;
    }

    public void updateEntity(CompanyDocument document, String filePath) {
        if (document == null) return;
        if (this.documentType != null) document.setDocumentType(this.documentType);
        if (this.documentNumber != null) document.setDocumentNumber(this.documentNumber);
        if (this.expiryDate != null) document.setExpiryDate(this.expiryDate);
        if (this.verificationStatus != null) document.setVerificationStatus(this.verificationStatus);
        if (this.companyId != null) document.setCompanyId(this.companyId);
        if (filePath != null) document.setFilePath(filePath);
    }
}

