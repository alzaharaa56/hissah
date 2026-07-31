package com.hissah.Services.Implementations.Support;

import com.hissah.Entities.Company;
import com.hissah.Entities.Project;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyReferenceSupport {

    private final CompanyRepository companyRepository;

    public Company requireCompany(Long companyId) {
        if (companyId == null) {
            throw new ResourceNotFoundException(
                    "The referenced company ID is missing."
            );
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + companyId
                ));
    }

    public Company requireProjectContractor(Project project) {
        if (project == null || project.getContractorCompanyId() == null) {
            throw new ResourceNotFoundException(
                    "The project contractor could not be identified."
            );
        }
        return requireCompany(project.getContractorCompanyId());
    }

    public Long companyUserId(Company company) {
        return company == null ? null : company.getUserId();
    }

    public String companyName(Company company) {
        if (company == null) {
            return null;
        }
        if (company.getTradingName() != null
                && !company.getTradingName().isBlank()) {
            return company.getTradingName();
        }
        return company.getLegalName();
    }
}
