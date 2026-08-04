package com.hissah.DTO.Response;

import com.hissah.Entities.Company;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.VerificationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyResponseDTO {

    private Long id;
    private String legalName;
    private String tradingName;
    private CompanyType companyType;
    private String crNumber;
    private String governorate;
    private String description;
    private VerificationStatus verificationStatus;
    private String rejectionReason;
    private Long userId;
    private Boolean active;
    private List<CompanyDocumentResponseDTO> documents = new ArrayList<>();
    private List<CategoryResponseDTO> categories = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyResponseDTO fromEntity(Company company) {
        if (company == null) {
            return null;
        }

        CompanyResponseDTO response = new CompanyResponseDTO();
        response.setId(company.getId());
        response.setLegalName(company.getLegalName());
        response.setTradingName(company.getTradingName());
        response.setCompanyType(company.getCompanyType());
        response.setCrNumber(company.getCrNumber());
        response.setGovernorate(company.getGovernorate());
        response.setDescription(company.getDescription());
        response.setVerificationStatus(company.getVerificationStatus());
        response.setRejectionReason(company.getRejectionReason());
        response.setUserId(company.getUserId());
        response.setActive(company.getActive());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }
}
