package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.WorkPackageRequestDTO;
import com.hissah.DTO.Request.WorkPackageSearchRequestDTO;
import com.hissah.DTO.Response.WorkPackageResponseDTO;
import com.hissah.DTO.Response.WorkPackageSummaryResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Entities.Company;
import com.hissah.Entities.Project;
import com.hissah.Entities.StatusHistory;
import com.hissah.Entities.User;
import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.HistoryEntityType;
import com.hissah.Enums.WorkPackageStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.BidRepository;
import com.hissah.Repositories.CategoryRepository;
import com.hissah.Repositories.ProjectRepository;
import com.hissah.Repositories.StatusHistoryRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Repositories.WorkPackageRepository;
import com.hissah.Repositories.Specifications.WorkPackageSpecification;
import com.hissah.Services.Implementations.Support.CompanyReferenceSupport;
import com.hissah.Services.WorkPackageService;
import com.hissah.Utilities.OwnershipValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkPackageServiceImpl implements WorkPackageService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "publishedAt",
            "createdAt",
            "deadline",
            "budgetMin",
            "budgetMax",
            "title"
    );

    private final WorkPackageRepository workPackageRepository;
    private final BidRepository bidRepository;
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final OwnershipValidator ownershipValidator;
    private final CompanyReferenceSupport companyReferenceSupport;

    @Override
    @Transactional
    public WorkPackageResponseDTO create(
            WorkPackageRequestDTO request,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Project project = getProject(request.getProjectId());
        ownershipValidator.validateProjectOwnership(currentCompanyId, project);
        validateContractorCompany(companyReferenceSupport.requireProjectContractor(project));
        Category category = getCategory(request.getCategoryId());
        validateRequest(request);

        if (workPackageRepository
                .existsByProjectIdAndTitleIgnoreCaseAndActiveTrue(
                        project.getId(), request.getTitle().trim())) {
            throw new BusinessRuleException(
                    "A work package with the same title already exists under this project."
            );
        }

        WorkPackage workPackage = new WorkPackage();
        workPackage.setReferenceNumber(generateReference("WPK"));
        workPackage.setStatus(WorkPackageStatus.DRAFT);
        workPackage.setProject(project);
        workPackage.setCategory(category);
        applyRequest(workPackage, request);

        WorkPackage saved = workPackageRepository.save(workPackage);
        recordHistory(
                saved.getId(),
                null,
                saved.getStatus().name(),
                currentUserId,
                "Work package created as draft."
        );
        return toResponse(saved, currentCompanyId);
    }

    @Override
    @Transactional
    public WorkPackageResponseDTO update(
            Long workPackageId,
            WorkPackageRequestDTO request,
            Long currentCompanyId,
            Long currentUserId
    ) {
        WorkPackage workPackage =
                getOwnedWorkPackage(workPackageId, currentCompanyId, false);

        validateContractorCompany(
                companyReferenceSupport.requireProjectContractor(workPackage.getProject())
        );

        if (workPackage.getStatus() != WorkPackageStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only draft work packages can be edited."
            );
        }

        Project project = getProject(request.getProjectId());
        ownershipValidator.validateProjectOwnership(currentCompanyId, project);
        validateContractorCompany(companyReferenceSupport.requireProjectContractor(project));
        Category category = getCategory(request.getCategoryId());
        validateRequest(request);

        workPackage.setProject(project);
        workPackage.setCategory(category);
        applyRequest(workPackage, request);

        WorkPackage saved = workPackageRepository.save(workPackage);
        recordHistory(
                saved.getId(),
                WorkPackageStatus.DRAFT.name(),
                WorkPackageStatus.DRAFT.name(),
                currentUserId,
                "Draft work package details updated."
        );
        return toResponse(saved, currentCompanyId);
    }

    @Override
    @Transactional
    public WorkPackageResponseDTO publish(
            Long workPackageId,
            Long currentCompanyId,
            Long currentUserId
    ) {
        WorkPackage workPackage =
                getOwnedWorkPackage(workPackageId, currentCompanyId, true);

        validateContractorCompany(
                companyReferenceSupport.requireProjectContractor(workPackage.getProject())
        );

        if (workPackage.getStatus() != WorkPackageStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only a draft work package can be published."
            );
        }
        if (!workPackage.getDeadline().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "The work package deadline must be in the future."
            );
        }

        WorkPackageStatus oldStatus = workPackage.getStatus();
        workPackage.setStatus(WorkPackageStatus.OPEN);
        workPackage.setPublishedAt(LocalDateTime.now());

        WorkPackage saved = workPackageRepository.save(workPackage);
        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Work package published for SME bidding."
        );
        return toResponse(saved, currentCompanyId);
    }

    @Override
    @Transactional
    public WorkPackageResponseDTO close(
            Long workPackageId,
            Long currentCompanyId,
            Long currentUserId
    ) {
        WorkPackage workPackage =
                getOwnedWorkPackage(workPackageId, currentCompanyId, true);

        if (workPackage.getStatus() != WorkPackageStatus.OPEN) {
            throw new BusinessRuleException(
                    "Only an open work package can be closed."
            );
        }

        WorkPackageStatus oldStatus = workPackage.getStatus();
        workPackage.setStatus(WorkPackageStatus.CLOSED);
        workPackage.setClosedAt(LocalDateTime.now());

        WorkPackage saved = workPackageRepository.save(workPackage);
        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Work package closed for new bids."
        );
        return toResponse(saved, currentCompanyId);
    }

    @Override
    @Transactional
    public WorkPackageResponseDTO cancel(
            Long workPackageId,
            String reason,
            Long currentCompanyId,
            Long currentUserId
    ) {
        WorkPackage workPackage =
                getOwnedWorkPackage(workPackageId, currentCompanyId, true);

        if (workPackage.getStatus() == WorkPackageStatus.AWARDED) {
            throw new BusinessRuleException(
                    "An awarded work package cannot be cancelled."
            );
        }
        if (workPackage.getStatus() == WorkPackageStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "The work package is already cancelled."
            );
        }

        WorkPackageStatus oldStatus = workPackage.getStatus();
        workPackage.setStatus(WorkPackageStatus.CANCELLED);
        workPackage.setClosedAt(LocalDateTime.now());

        WorkPackage saved = workPackageRepository.save(workPackage);
        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                reason == null || reason.isBlank()
                        ? "Work package cancelled."
                        : reason.trim()
        );
        return toResponse(saved, currentCompanyId);
    }

    @Override
    public WorkPackageResponseDTO getById(
            Long workPackageId,
            Long viewerCompanyId
    ) {
        return toResponse(getWorkPackage(workPackageId), viewerCompanyId);
    }

    @Override
    public List<WorkPackageSummaryResponseDTO> getByProject(
            Long projectId,
            Long currentCompanyId
    ) {
        Project project = getProject(projectId);
        ownershipValidator.validateProjectOwnership(currentCompanyId, project);

        return workPackageRepository
                .findByProjectIdAndActiveTrueOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public Page<WorkPackageSummaryResponseDTO> search(
            WorkPackageSearchRequestDTO request
    ) {
        WorkPackageSearchRequestDTO safeRequest =
                request == null
                        ? WorkPackageSearchRequestDTO.builder().build()
                        : request;

        if (safeRequest.getStatus() == null) {
            safeRequest.setStatus(WorkPackageStatus.OPEN);
        }

        String sortBy = ALLOWED_SORT_FIELDS.contains(safeRequest.getSortBy())
                ? safeRequest.getSortBy()
                : "publishedAt";

        Sort.Direction direction =
                "ASC".equalsIgnoreCase(safeRequest.getSortDirection())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(
                safeRequest.getPage() == null ? 0 : safeRequest.getPage(),
                safeRequest.getSize() == null ? 10 : safeRequest.getSize(),
                Sort.by(direction, sortBy)
        );

        return workPackageRepository
                .findAll(WorkPackageSpecification.from(safeRequest), pageable)
                .map(this::toSummary);
    }

    private WorkPackage getWorkPackage(Long id) {
        return workPackageRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work package not found with id: " + id
                ));
    }

    private WorkPackage getOwnedWorkPackage(
            Long id,
            Long companyId,
            boolean lock
    ) {
        WorkPackage workPackage =
                (lock
                        ? workPackageRepository.findByIdForUpdate(id)
                        : workPackageRepository.findByIdAndActiveTrue(id))
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Work package not found with id: " + id
                        ));

        ownershipValidator.validateWorkPackageOwnership(
                companyId,
                workPackage
        );
        return workPackage;
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + id
                ));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id
                ));
    }

    private void validateContractorCompany(Company company) {
        if (company == null) {
            throw new BusinessRuleException(
                    "The project is not linked to a contractor company."
            );
        }
        if (company.getVerificationStatus() == null
                || !"VERIFIED".equals(
                company.getVerificationStatus().name())) {
            throw new BusinessRuleException(
                    "Only a verified contractor can manage work packages."
            );
        }

        String companyType = company.getCompanyType() == null
                ? ""
                : company.getCompanyType().name();

        if (!"MAIN_CONTRACTOR".equals(companyType)
                && !"BOTH".equals(companyType)) {
            throw new BusinessRuleException(
                    "Only a main contractor or dual-role company can manage work packages."
            );
        }
    }

    private void validateRequest(WorkPackageRequestDTO request) {
        if (request.getBudgetMax().compareTo(request.getBudgetMin()) < 0) {
            throw new BusinessRuleException(
                    "Maximum budget must be greater than or equal to minimum budget."
            );
        }
        if (!request.getDeadline().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Deadline must be in the future."
            );
        }
    }

    private void applyRequest(
            WorkPackage workPackage,
            WorkPackageRequestDTO request
    ) {
        workPackage.setTitle(request.getTitle().trim());
        workPackage.setScope(request.getScope().trim());
        workPackage.setRequirements(trimToNull(request.getRequirements()));
        workPackage.setBudgetMin(request.getBudgetMin());
        workPackage.setBudgetMax(request.getBudgetMax());
        workPackage.setDeadline(request.getDeadline());
        workPackage.setLocation(request.getLocation().trim());
        workPackage.setEligibilityType(request.getEligibilityType());
    }

    private void recordHistory(
            Long entityId,
            String oldStatus,
            String newStatus,
            Long userId,
            String note
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        StatusHistory history = new StatusHistory();
        history.setEntityType(HistoryEntityType.WORK_PACKAGE);
        history.setEntityId(entityId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(user);
        history.setNote(note);

        statusHistoryRepository.save(history);
    }

    private WorkPackageResponseDTO toResponse(
            WorkPackage workPackage,
            Long viewerCompanyId
    ) {
        Project project = workPackage.getProject();
        Category category = workPackage.getCategory();
        Company contractor = companyReferenceSupport.requireProjectContractor(project);

        long bidCount =
                bidRepository.countByWorkPackageIdAndActiveTrue(
                        workPackage.getId()
                );

        boolean canBid =
                viewerCompanyId != null
                        && contractor != null
                        && !viewerCompanyId.equals(contractor.getId())
                        && workPackage.getStatus() == WorkPackageStatus.OPEN
                        && workPackage.getDeadline().isAfter(LocalDateTime.now());

        return WorkPackageResponseDTO.builder()
                .id(workPackage.getId())
                .referenceNumber(workPackage.getReferenceNumber())
                .title(workPackage.getTitle())
                .scope(workPackage.getScope())
                .requirements(workPackage.getRequirements())
                .budgetMin(workPackage.getBudgetMin())
                .budgetMax(workPackage.getBudgetMax())
                .deadline(workPackage.getDeadline())
                .location(workPackage.getLocation())
                .status(workPackage.getStatus())
                .eligibilityType(workPackage.getEligibilityType())
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .projectReferenceNumber(project.getReferenceNumber())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .contractorCompanyId(
                        contractor == null ? null : contractor.getId()
                )
                .contractorCompanyName(companyName(contractor))
                .bidCount(bidCount)
                .canBid(canBid)
                .publishedAt(workPackage.getPublishedAt())
                .closedAt(workPackage.getClosedAt())
                .createdAt(workPackage.getCreatedAt())
                .updatedAt(workPackage.getUpdatedAt())
                .build();
    }

    private WorkPackageSummaryResponseDTO toSummary(
            WorkPackage workPackage
    ) {
        Project project = workPackage.getProject();
        Category category = workPackage.getCategory();
        Company contractor = companyReferenceSupport.requireProjectContractor(project);

        return WorkPackageSummaryResponseDTO.builder()
                .id(workPackage.getId())
                .referenceNumber(workPackage.getReferenceNumber())
                .title(workPackage.getTitle())
                .budgetMin(workPackage.getBudgetMin())
                .budgetMax(workPackage.getBudgetMax())
                .deadline(workPackage.getDeadline())
                .location(workPackage.getLocation())
                .status(workPackage.getStatus())
                .eligibilityType(workPackage.getEligibilityType())
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .contractorCompanyId(
                        contractor == null ? null : contractor.getId()
                )
                .contractorCompanyName(companyName(contractor))
                .bidCount(
                        bidRepository.countByWorkPackageIdAndActiveTrue(
                                workPackage.getId()
                        )
                )
                .publishedAt(workPackage.getPublishedAt())
                .build();
    }

    private String companyName(Company company) {
        return companyReferenceSupport.companyName(company);
    }

    private String generateReference(String prefix) {
        return prefix
                + "-"
                + LocalDateTime.now().getYear()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
