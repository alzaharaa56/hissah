package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.LoginRequestDTO;
import com.hissah.DTO.Request.RegisterRequestDTO;
import com.hissah.DTO.Response.AuthResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Entities.Company;
import com.hissah.Entities.CompanyCategory;
import com.hissah.Entities.User;
import com.hissah.Enums.AccountStatus;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.Role;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.DuplicateResourceException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CategoryRepository;
import com.hissah.Repositories.CompanyCategoryRepository;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Security.CustomUserDetailsService;
import com.hissah.Security.JwtService;
import com.hissah.Services.AuthService;
import com.hissah.Services.Implementations.Support.ServiceDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyCategoryRepository companyCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final ServiceDtoMapper mapper;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String fullName = required(
                mapper.text(request, "fullName"),
                "Full name"
        );
        String email = normalizeEmail(
                required(mapper.text(request, "email"), "Email")
        );
        String password = required(
                mapper.text(request, "password"),
                "Password"
        );
        String phone = required(
                mapper.text(request, "phone"),
                "Phone"
        );

        String legalName = required(
                mapper.text(
                        request,
                        "legalName",
                        "companyLegalName",
                        "company.legalName"
                ),
                "Company legal name"
        );
        String tradingName = mapper.text(
                request,
                "tradingName",
                "companyTradingName",
                "company.tradingName"
        );
        String crNumber = required(
                mapper.text(
                        request,
                        "crNumber",
                        "companyCrNumber",
                        "company.crNumber"
                ),
                "Commercial registration number"
        );
        String governorate = required(
                mapper.text(
                        request,
                        "governorate",
                        "company.governorate"
                ),
                "Governorate"
        );
        String description = mapper.text(
                request,
                "description",
                "companyDescription",
                "company.description"
        );

        Role role = mapper.enumValue(
                request,
                Role.class,
                "role"
        );
        CompanyType companyType = mapper.enumValue(
                request,
                CompanyType.class,
                "companyType",
                "company.companyType"
        );

        role = resolveRole(role, companyType);
        companyType = resolveCompanyType(companyType, role);
        validateRoleAndCompanyType(role, companyType);
        validatePassword(password);

        if (emailExists(email)) {
            throw new DuplicateResourceException(
                    "An account already exists with this email."
            );
        }
        if (crNumberExists(crNumber)) {
            throw new DuplicateResourceException(
                    "A company already exists with this CR number."
            );
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        Company company = new Company();
        company.setLegalName(legalName);
        company.setTradingName(tradingName);
        company.setCompanyType(companyType);
        company.setCrNumber(crNumber);
        company.setGovernorate(governorate);
        company.setDescription(description);
        company.setVerificationStatus(
                VerificationStatus.PENDING_REVIEW
        );
        Company savedCompany = companyRepository.save(company);

        assignCategories(
                savedCompany,
                mapper.longList(
                        request,
                        "categoryIds",
                        "company.categoryIds"
                )
        );

        UserDetails userDetails =
                customUserDetailsService
                        .loadUserByUsername(email);

        String token = jwtService.generateToken(userDetails);

        return buildAuthResponse(
                token,
                savedUser,
                savedCompany
        );
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        String email = normalizeEmail(
                required(mapper.text(request, "email"), "Email")
        );
        String password = required(
                mapper.text(request, "password"),
                "Password"
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        User user = findUserByEmail(email);

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "This account is not active."
            );
        }

        Company company = companyRepository.findAll().stream().findFirst().orElse(null);

        UserDetails userDetails =
                customUserDetailsService
                        .loadUserByUsername(email);

        String token = jwtService.generateToken(userDetails);

        return buildAuthResponse(token, user, company);
    }

    private AuthResponseDTO buildAuthResponse(
            String token,
            User user,
            Company company
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("accessToken", token);
        response.put("tokenType", "Bearer");
        response.put("role", user.getRole());
        response.put("user", userMap(user));
        response.put("company", company != null ? companySummaryMap(company) : null);
        return mapper.toDto(response, AuthResponseDTO.class);
    }

    private Map<String, Object> userMap(User user) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", user.getId());
        values.put("fullName", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("role", user.getRole());
        values.put("accountStatus", user.getAccountStatus());
        values.put("active", true);
        values.put("createdAt", LocalDateTime.now());
        values.put("updatedAt", LocalDateTime.now());
        return values;
    }

    private Map<String, Object> companySummaryMap(Company company) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", company.getId());
        values.put("legalName", company.getLegalName());
        values.put("tradingName", company.getTradingName());
        values.put("companyType", company.getCompanyType());
        values.put("crNumber", company.getCrNumber());
        values.put("governorate", company.getGovernorate());
        values.put(
                "verificationStatus",
                company.getVerificationStatus()
        );
        return values;
    }

    private void assignCategories(
            Company company,
            List<Long> categoryIds
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        List<CompanyCategory> relations = new ArrayList<>();

        for (Long categoryId : categoryIds.stream().distinct().toList()) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + categoryId
                    ));

            if (!Boolean.TRUE.equals(category.getActive())) {
                throw new BusinessRuleException(
                        "Inactive categories cannot be assigned."
                );
            }

            CompanyCategory relation = new CompanyCategory();
            relations.add(relation);
        }

        companyCategoryRepository.saveAll(relations);
    }

    private boolean emailExists(String email) {
        return userRepository.findAll()
                .stream()
                .anyMatch(user ->
                        user.getEmail() != null
                                && user.getEmail()
                                .equalsIgnoreCase(email)
                );
    }

    private boolean crNumberExists(String crNumber) {
        return companyRepository.findAll()
                .stream()
                .anyMatch(company ->
                        company.getCrNumber() != null
                                && company.getCrNumber()
                                .equalsIgnoreCase(crNumber)
                );
    }

    private User findUserByEmail(String email) {
        return userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getEmail() != null
                                && user.getEmail()
                                .equalsIgnoreCase(email)
                )
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User account not found."
                ));
    }

    private Role resolveRole(
            Role role,
            CompanyType companyType
    ) {
        if (role != null) {
            return role;
        }
        if (companyType == CompanyType.MAIN_CONTRACTOR) {
            return Role.MAIN_CONTRACTOR;
        }
        if (companyType == CompanyType.SUBCONTRACTOR) {
            return Role.SUBCONTRACTOR;
        }
        throw new BusinessRuleException(
                "A role is required when the company type is BOTH."
        );
    }

    private CompanyType resolveCompanyType(
            CompanyType companyType,
            Role role
    ) {
        if (companyType != null) {
            return companyType;
        }
        return role == Role.MAIN_CONTRACTOR
                ? CompanyType.MAIN_CONTRACTOR
                : CompanyType.SUBCONTRACTOR;
    }

    private void validateRoleAndCompanyType(
            Role role,
            CompanyType companyType
    ) {
        if (role == Role.ADMIN) {
            throw new BusinessRuleException(
                    "Administrator accounts cannot be self-registered."
            );
        }

        boolean valid =
                role == Role.MAIN_CONTRACTOR
                        && (
                        companyType == CompanyType.MAIN_CONTRACTOR
                                || companyType == CompanyType.BOTH
                )
                        || role == Role.SUBCONTRACTOR
                        && (
                        companyType == CompanyType.SUBCONTRACTOR
                                || companyType == CompanyType.BOTH
                );

        if (!valid) {
            throw new BusinessRuleException(
                    "The selected role does not match the company type."
            );
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new BusinessRuleException(
                    "Password must contain at least 8 characters."
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(
                    fieldName + " is required."
            );
        }
        return value.trim();
    }
}