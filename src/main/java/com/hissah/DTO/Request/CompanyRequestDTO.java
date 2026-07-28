package com.hissah.DTO.Request;

import com.hissah.Entities.Company;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.VerificationStatus;
import lombok.Data;

@Data
public class CompanyRequestDTO {
    private String legalName;
    private String tradingName;
    private CompanyType companyType;
    private String crNumber;
    private String governorate;
    private String description;
    private VerificationStatus verificationStatus;
    private Long userId;

    public Company toEntity() {
        Company company = new Company();
        company.setLegalName(this.legalName);
        company.setTradingName(this.tradingName);
        company.setCompanyType(this.companyType);
        company.setCrNumber(this.crNumber);
        company.setGovernorate(this.governorate);
        company.setDescription(this.description);
        company.setVerificationStatus(this.verificationStatus);
        company.setUserId(this.userId);
        return company;
    }

    public void updateEntity(Company company) {
        if (company == null) return;
        if (this.legalName != null) company.setLegalName(this.legalName);
        if (this.tradingName != null) company.setTradingName(this.tradingName);
        if (this.companyType != null) company.setCompanyType(this.companyType);
        if (this.crNumber != null) company.setCrNumber(this.crNumber);
        if (this.governorate != null) company.setGovernorate(this.governorate);
        if (this.description != null) company.setDescription(this.description);
        if (this.verificationStatus != null) company.setVerificationStatus(this.verificationStatus);
        if (this.userId != null) company.setUserId(this.userId);
    }
}