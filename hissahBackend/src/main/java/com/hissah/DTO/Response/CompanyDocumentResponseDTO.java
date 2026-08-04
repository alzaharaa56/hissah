package com.hissah.DTO.Response;

import com.hissah.Entities.CompanyDocument;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.VerificationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompanyDocumentResponseDTO {
    private Long id;
    private DocumentType documentType;
    private String documentNumber;
    private LocalDate expiryDate;
    private VerificationStatus verificationStatus;
    private Long companyId;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyDocumentResponseDTO fromEntity(CompanyDocument document) {
        if (document == null) return null;
        CompanyDocumentResponseDTO response = new CompanyDocumentResponseDTO();
        response.setId(document.getId());
        response.setDocumentType(document.getDocumentType());
        response.setDocumentNumber(document.getDocumentNumber());
        response.setExpiryDate(document.getExpiryDate());
        response.setVerificationStatus(document.getVerificationStatus());
        response.setCompanyId(document.getCompanyId());

        response.setDownloadUrl("/api/companies/documents/" + document.getId() + "/download");
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        return response;
    }
}
