package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.CompanyRequestDTO;
import com.hissah.DTO.Request.CompanyVerificationRequestDTO;
import com.hissah.DTO.Response.CompanyResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Entities.Company;
import com.hissah.Entities.CompanyCategory;
import com.hissah.Entities.CompanyDocument;
import com.hissah.Entities.StatusHistory;
import com.hissah.Entities.User;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.HistoryEntityType;
import com.hissah.Enums.NotificationType;
import com.hissah.Enums.Role;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.DuplicateResourceException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.CategoryRepository;
import com.hissah.Repositories.CompanyCategoryRepository;
import com.hissah.Repositories.CompanyDocumentRepository;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.StatusHistoryRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Services.CompanyService;
import com.hissah.Services.NotificationService;
import com.hissah.Services.Implementations.Support.ServiceDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyDocumentRepository companyDocumentRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyCategoryRepository companyCategoryRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ServiceDtoMapper mapper;

    @Override
    public CompanyResponseDTO getCurrentCompany(Long userId) {
        return toResponse(findCompanyByUserId(userId));
    }

    @Override
    public CompanyResponseDTO getById(
            Long companyId,
            Long currentCompanyId,
            Role currentRole
    ) {
        Company company = getCompany(companyId);

        boolean admin = currentRole == Role.ADMIN;
        boolean owner = Objects.equals(companyId, currentCompanyId);

        if (!admin && !owner) {
            throw new UnauthorizedOperationException(
                    "You cannot view another company's private profile."
            );
        }

        return toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponseDTO updateCurrentCompany(
            Long companyId,
            CompanyRequestDTO request
    ) {
        Company company = getCompany(companyId);

        String legalName = mapper.text(
                request,
                "legalName",
                "companyLegalName"
        );
        String tradingName = mapper.text(
                request,
                "tradingName",
                "companyTradingName"
        );
        String crNumber = mapper.text(
                request,
                "crNumber",
                "companyCrNumber"
        );
        String governorate = mapper.text(
                request,
                "governorate"
        );
        String description = mapper.text(
                request,
                "description"
        );
        CompanyType companyType = mapper.enumValue(
                request,
                CompanyType.class,
                "companyType"
        );

        boolean verificationSensitiveChange = false;

        if (legalName != null
                && !legalName.equals(company.getLegalName())) {
            company.setLegalName(legalName);
            verificationSensitiveChange = true;
        }
        if (tradingName != null) {
            company.setTradingName(tradingName);
        }
        if (crNumber != null
                && !crNumber.equalsIgnoreCase(
                company.getCrNumber()
        )) {
            ensureCrNumberAvailable(crNumber, companyId);
            company.setCrNumber(crNumber);
            verificationSensitiveChange = true;
        }
        if (governorate != null
                && !governorate.equals(
                company.getGovernorate()
        )) {
            company.setGovernorate(governorate);
            verificationSensitiveChange = true;
        }
        if (description != null) {
            company.setDescription(description);
        }
        if (companyType != null
                && companyType != company.getCompanyType()) {
            validateCompanyTypeAgainstRole(
                    companyType,
                    company.getUser().getRole()
            );
            company.setCompanyType(companyType);
            verificationSensitiveChange = true;
        }

        if (verificationSensitiveChange) {
            company.setVerificationStatus(
                    VerificationStatus.PENDING_REVIEW
            );
            company.setRejectionReason(null);
        }

        Company saved = companyRepository.save(company);

        List<Long> categoryIds = mapper.longList(
                request,
                "categoryIds"
        );
        if (!categoryIds.isEmpty()) {
            replaceCategories(saved, categoryIds);
        }

        return toResponse(saved);
    }

    @Override
    public List<CompanyResponseDTO> getPendingCompanies() {
        return companyRepository.findAll()
                .stream()
                .filter(company ->
                        Boolean.TRUE.equals(company.getActive())
                                && company.getVerificationStatus()
                                == VerificationStatus.PENDING_REVIEW
                )
                .sorted(Comparator.comparing(
                        Company::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                ))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CompanyResponseDTO verifyCompany(
            Long companyId,
            CompanyVerificationRequestDTO request,
            Long adminUserId
    ) {
        Company company = getCompany(companyId);
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrator user not found."
                ));

        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedOperationException(
                    "Only an administrator can verify a company."
            );
        }

        VerificationStatus decision = mapper.enumValue(
                request,
                VerificationStatus.class,
                "verificationStatus",
                "status",
                "decision"
        );
        String reason = mapper.text(
                request,
                "reason",
                "rejectionReason"
        );

        if (decision != VerificationStatus.VERIFIED
                && decision != VerificationStatus.REJECTED) {
            throw new BusinessRuleException(
                    "Company verification decision must be VERIFIED or REJECTED."
            );
        }

        if (decision == VerificationStatus.VERIFIED) {
            validateRequiredCompanyDocuments(companyId);
            company.setRejectionReason(null);
        } else {
            if (reason == null) {
                throw new BusinessRuleException(
                        "A rejection reason is required."
                );
            }
            company.setRejectionReason(reason);
        }

        VerificationStatus oldStatus =
                company.getVerificationStatus();

        company.setVerificationStatus(decision);
        Company saved = companyRepository.save(company);

        StatusHistory history = new StatusHistory();
        history.setEntityType(HistoryEntityType.COMPANY);
        history.setEntityId(companyId);
        history.setOldStatus(
                oldStatus == null ? null : oldStatus.name()
        );
        history.setNewStatus(decision.name());
        history.setChangedBy(admin);
        history.setNote(
                reason == null
                        ? "Company verification approved."
                        : reason
        );
        statusHistoryRepository.save(history);

        User companyUser = company.getUser();
        if (companyUser != null) {
            notificationService.create(
                    companyUser.getId(),
                    decision == VerificationStatus.VERIFIED
                            ? "Company verified"
                            : "Company verification rejected",
                    decision == VerificationStatus.VERIFIED
                            ? "Your company profile is now verified."
                            : "Your company verification was rejected: "
                            + reason,
                    NotificationType.VERIFICATION
            );
        }

        return toResponse(saved);
    }

    @Override
    public void ensureVerifiedMainContractor(Long companyId) {
        Company company = getCompany(companyId);
        ensureVerified(company);

        if (company.getCompanyType()
                != CompanyType.MAIN_CONTRACTOR
                && company.getCompanyType()
                != CompanyType.BOTH) {
            throw new BusinessRuleException(
                    "Only a verified main contractor can perform this action."
            );
        }
    }

    @Override
    public void ensureVerifiedSubcontractor(Long companyId) {
        Company company = getCompany(companyId);
        ensureVerified(company);

        if (company.getCompanyType()
                != CompanyType.SUBCONTRACTOR
                && company.getCompanyType()
                != CompanyType.BOTH) {
            throw new BusinessRuleException(
                    "Only a verified subcontractor can perform this action."
            );
        }
    }

    private void ensureVerified(Company company) {
        if (!Boolean.TRUE.equals(company.getActive())) {
            throw new BusinessRuleException(
                    "The company profile is inactive."
            );
        }
        if (company.getVerificationStatus()
                != VerificationStatus.VERIFIED) {
            throw new BusinessRuleException(
                    "The company must be verified before performing this action."
            );
        }
    }

    private void validateRequiredCompanyDocuments(
            Long companyId
    ) {
        List<CompanyDocument> documents =
                activeDocuments(companyId);

        boolean commercialRegistrationExists =
                documents.stream()
                        .anyMatch(document ->
                                document.getDocumentType()
                                        == DocumentType.COMMERCIAL_REGISTRATION
                                        && !isExpired(document)
                        );

        if (!commercialRegistrationExists) {
            throw new BusinessRuleException(
                    "A valid Commercial Registration document is required before verification."
            );
        }
    }

    private boolean isExpired(CompanyDocument document) {
        return document.getExpiryDate() != null
                && document.getExpiryDate()
                .isBefore(LocalDate.now());
    }

    private void replaceCategories(
            Company company,
            List<Long> categoryIds
    ) {
        List<CompanyCategory> existing =
                companyCategoryRepository.findAll()
                        .stream()
                        .filter(relation ->
                                relation.getCompany() != null
                                        && company.getId().equals(
                                        relation.getCompany().getId()
                                )
                        )
                        .toList();

        companyCategoryRepository.deleteAll(existing);

        List<CompanyCategory> replacements =
                new ArrayList<>();

        for (Long categoryId :
                categoryIds.stream().distinct().toList()) {
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
            relation.setCompany(company);
            relation.setCategory(category);
            replacements.add(relation);
        }

        companyCategoryRepository.saveAll(replacements);
    }

    private void ensureCrNumberAvailable(
            String crNumber,
            Long currentCompanyId
    ) {
        boolean duplicate = companyRepository.findAll()
                .stream()
                .anyMatch(company ->
                        !company.getId().equals(currentCompanyId)
                                && company.getCrNumber() != null
                                && company.getCrNumber()
                                .equalsIgnoreCase(crNumber)
                );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Another company already uses this CR number."
            );
        }
    }

    private void validateCompanyTypeAgainstRole(
            CompanyType companyType,
            Role role
    ) {
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
                    "The company type does not match the account role."
            );
        }
    }

    private Company findCompanyByUserId(Long userId) {
        return companyRepository.findAll()
                .stream()
                .filter(company ->
                        company.getUser() != null
                                && userId.equals(
                                company.getUser().getId()
                        )
                )
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company profile not found for the current user."
                ));
    }

    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + companyId
                ));
    }

    private List<CompanyDocument> activeDocuments(
            Long companyId
    ) {
        return companyDocumentRepository.findAll()
                .stream()
                .filter(document ->
                        document.getCompany() != null
                                && companyId.equals(
                                document.getCompany().getId()
                        )
                                && Boolean.TRUE.equals(
                                document.getActive()
                        )
                )
                .toList();
    }

    private List<Category> assignedCategories(Long companyId) {
        return companyCategoryRepository.findAll()
                .stream()
                .filter(relation ->
                        relation.getCompany() != null
                                && companyId.equals(
                                relation.getCompany().getId()
                        )
                )
                .map(CompanyCategory::getCategory)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Category::getName))
                .toList();
    }

    private CompanyResponseDTO toResponse(Company company) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", company.getId());
        values.put("legalName", company.getLegalName());
        values.put("tradingName", company.getTradingName());
        values.put("companyType", company.getCompanyType());
        values.put("crNumber", company.getCrNumber());
        values.put("governorate", company.getGovernorate());
        values.put("description", company.getDescription());
        values.put(
                "verificationStatus",
                company.getVerificationStatus()
        );
        values.put(
                "rejectionReason",
                company.getRejectionReason()
        );
        values.put("active", company.getActive());
        values.put("createdAt", company.getCreatedAt());
        values.put("updatedAt", company.getUpdatedAt());
        values.put("user", userMap(company.getUser()));
        values.put(
                "documents",
                activeDocuments(company.getId())
                        .stream()
                        .map(this::documentMap)
                        .toList()
        );
        values.put(
                "categories",
                assignedCategories(company.getId())
                        .stream()
                        .map(this::categoryMap)
                        .toList()
        );
        return mapper.toDto(values, CompanyResponseDTO.class);
    }

    private Map<String, Object> userMap(User user) {
        if (user == null) {
            return null;
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", user.getId());
        values.put("fullName", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("role", user.getRole());
        values.put("accountStatus", user.getAccountStatus());
        return values;
    }

    private Map<String, Object> documentMap(
            CompanyDocument document
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", document.getId());
        values.put(
                "documentType",
                document.getDocumentType()
        );
        values.put(
                "documentNumber",
                document.getDocumentNumber()
        );
        values.put("fileName", document.getFileName());
        values.put("expiryDate", document.getExpiryDate());
        values.put(
                "verificationStatus",
                document.getVerificationStatus()
        );
        values.put("createdAt", document.getCreatedAt());
        return values;
    }

    private Map<String, Object> categoryMap(Category category) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", category.getId());
        values.put("name", category.getName());
        values.put("description", category.getDescription());
        values.put("active", category.getActive());
        values.put(
                "parentCategoryId",
                category.getParentCategory() == null
                        ? null
                        : category.getParentCategory().getId()
        );
        return values;
    }
}
