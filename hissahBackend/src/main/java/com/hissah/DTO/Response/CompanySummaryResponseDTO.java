package com.hissah.DTO.Response;

import com.hissah.Entities.Company;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.VerificationStatus;
import lombok.Data;

@Data
public class CompanySummaryResponseDTO {
    private Long id;
    private String legalName;
    private String tradingName;
    private CompanyType companyType;
    private String crNumber;
    private String governorate;
    private VerificationStatus verificationStatus;

    public static CompanySummaryResponseDTO fromEntity(Company company) {
        if (company == null) return null;
        CompanySummaryResponseDTO summary = new CompanySummaryResponseDTO();
        summary.setId(company.getId());
        summary.setLegalName(company.getLegalName());
        summary.setTradingName(company.getTradingName());
        summary.setCompanyType(company.getCompanyType());
        summary.setCrNumber(company.getCrNumber());
        summary.setGovernorate(company.getGovernorate());
        summary.setVerificationStatus(company.getVerificationStatus());
        return summary;
    }
}
