package com.hissah.Services;


import com.hissah.DTO.Request.CompanyRequestDTO;
import com.hissah.DTO.Response.CompanyResponseDTO;
import com.hissah.Entities.Company;
import com.hissah.Entities.CompanyCategory;
import com.hissah.Entities.User;
import com.hissah.Exceptions.ResourceNotFoundException;
//import com.hissah.Exceptions.UnauthorizedAccessException;
import com.hissah.Repositories.CompanyCategoryRepository;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyCategoryRepository companyCategoryRepository;
    private final UserRepository userRepository;


    @Transactional
    public CompanyResponseDTO createCompany(CompanyRequestDTO request) {
        Company company = request.toEntity();


        Company savedCompany = companyRepository.save(company);

        return CompanyResponseDTO.fromEntity(savedCompany);
    }

    public Company getCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
    }


    public CompanyResponseDTO getCompanyProfile(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        return CompanyResponseDTO.fromEntity(company);
    }


    @Transactional
    public CompanyResponseDTO updateCompanyProfile(Long companyId, String currentUserEmail, CompanyRequestDTO request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));


        User currentUser = userRepository.findUserByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));


        if (company.getUserId() != null && !company.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Access Denied: You are not authorized to update this company profile.");
        }


        request.updateEntity(company);


        Company updatedCompany = companyRepository.save(company);

        return CompanyResponseDTO.fromEntity(updatedCompany);
    }
}
