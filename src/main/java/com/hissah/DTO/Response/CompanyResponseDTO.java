package com.hissah.DTO.Response;

import com.hissah.DTO.Request.CompanyDocumentRequestDTO;
import com.hissah.Entities.Company;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.VerificationStatus;
import lombok.Data;

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
    private Long userId;

     private List<CompanyDocumentRequestDTO> documents;

    public static CompanyResponseDTO fromEntity(Company company) {
        if (company == null) return null;
        CompanyResponseDTO response = new CompanyResponseDTO();
        response.setId(company.getId());
        response.setLegalName(company.getLegalName());
        response.setTradingName(company.getTradingName());
        response.setCompanyType(company.getCompanyType());
        response.setCrNumber(company.getCrNumber());
        response.setGovernorate(company.getGovernorate());
        response.setDescription(company.getDescription());
        response.setVerificationStatus(company.getVerificationStatus());
        response.setUserId(company.getUserId());
        return response;
    }
}
