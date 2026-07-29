package com.hissah.Services.Implementations;


import com.hissah.Entities.Company;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;


    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }


    public Company getCompanyByUserId(Long userId) {
        return companyRepository.findCompanyByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for user id: " + userId));
    }


    public Company getCompanyByCrNumber(String crNumber) {
        return companyRepository.findCompanyByCrNumber(crNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with CR number: " + crNumber));
    }


    @Transactional
    public Company updateVerificationStatus(Long companyId, VerificationStatus status) {
        Company company = getCompanyById(companyId);
        company.setVerificationStatus(status);
        return companyRepository.save(company);
    }


    public boolean verifyCompanyOwnership(Long companyId, String userEmail) {
        Company company = getCompanyById(companyId);

        return userRepository.findById(company.getUserId())
                .map(user -> user.getEmail().equals(userEmail))
                .orElse(false);
    }


    public void validateCompanyIsVerified(Long companyId) {
        Company company = getCompanyById(companyId);
        if (company.getVerificationStatus() == null || company.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Access denied: Company must be verified to perform this action.");
        }
    }
}
