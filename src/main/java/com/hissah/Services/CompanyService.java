package com.hissah.Services;
import com.hissah.DTO.Request.CompanyRequestDTO;
import com.hissah.DTO.Request.CompanyVerificationRequestDTO;
import com.hissah.DTO.Response.CompanyResponseDTO;
import com.hissah.Enums.Role;

import java.util.List;

public interface CompanyService {
    CompanyResponseDTO getCurrentCompany(Long userId);

    CompanyResponseDTO getById(
            Long companyId,
            Long currentCompanyId,
            Role currentRole
    );

    CompanyResponseDTO updateCurrentCompany(
            Long companyId,
            CompanyRequestDTO request
    );

    List<CompanyResponseDTO> getPendingCompanies();

    CompanyResponseDTO verifyCompany(
            Long companyId,
            CompanyVerificationRequestDTO request,
            Long adminUserId
    );

    void ensureVerifiedMainContractor(Long companyId);

    void ensureVerifiedSubcontractor(Long companyId);
}
