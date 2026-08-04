package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.ProjectRequestDTO;
import com.hissah.DTO.Response.CompanySummaryResponseDTO;
import com.hissah.DTO.Response.ProjectResponseDTO;
import com.hissah.DTO.Response.ProjectSummaryResponseDTO;
import com.hissah.Entities.Company;
import com.hissah.Entities.Project;
import com.hissah.Entities.StatusHistory;
import com.hissah.Entities.User;
import com.hissah.Enums.HistoryEntityType;
import com.hissah.Enums.ProjectSector;
import com.hissah.Enums.ProjectStatus;
import com.hissah.Enums.Role;
import com.hissah.Enums.WorkPackageStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.DuplicateResourceException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.ProjectRepository;
import com.hissah.Repositories.StatusHistoryRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Repositories.WorkPackageRepository;
import com.hissah.Services.CompanyService;
import com.hissah.Services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final WorkPackageRepository workPackageRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;

    @Override
    @Transactional
    public ProjectResponseDTO create(
            ProjectRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        companyService.ensureVerifiedMainContractor(
                contractorCompanyId
        );

        Company contractor = getCompany(contractorCompanyId);
        ProjectData data = readProjectData(request);

        validateDates(data.startDate(), data.endDate());
        ensureTitleAvailable(
                contractorCompanyId,
                data.title(),
                null
        );

        Project project = new Project();
        project.setTitle(data.title());
        project.setReferenceNumber(generateReference());
        project.setSector(data.sector());
        project.setLocation(data.location());
        project.setDescription(data.description());
        project.setStartDate(data.startDate());
        project.setEndDate(data.endDate());
        project.setStatus(ProjectStatus.DRAFT);
        project.setContractorCompanyId(contractor.getId());
        project.setActive(true);

        Project saved = projectRepository.save(project);

        recordHistory(
                saved.getId(),
                null,
                saved.getStatus().name(),
                currentUserId,
                "Project created as draft."
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponseDTO update(
            Long projectId,
            ProjectRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        companyService.ensureVerifiedMainContractor(
                contractorCompanyId
        );

        Project project = getOwnedProjectEntity(
                projectId,
                contractorCompanyId
        );

        if (project.getStatus() == ProjectStatus.COMPLETED
                || project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "A completed or cancelled project cannot be edited."
            );
        }

        ProjectData data = readProjectData(request);

        validateDates(data.startDate(), data.endDate());
        ensureTitleAvailable(
                contractorCompanyId,
                data.title(),
                projectId
        );

        project.setTitle(data.title());
        project.setSector(data.sector());
        project.setLocation(data.location());
        project.setDescription(data.description());
        project.setStartDate(data.startDate());
        project.setEndDate(data.endDate());

        Project saved = projectRepository.save(project);

        recordHistory(
                saved.getId(),
                saved.getStatus().name(),
                saved.getStatus().name(),
                currentUserId,
                "Project details updated."
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponseDTO activate(
            Long projectId,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        companyService.ensureVerifiedMainContractor(
                contractorCompanyId
        );

        Project project = getOwnedProjectEntity(
                projectId,
                contractorCompanyId
        );

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only a draft project can be activated."
            );
        }

        if (project.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException(
                    "A project with a past end date cannot be activated."
            );
        }

        ProjectStatus oldStatus = project.getStatus();
        project.setStatus(ProjectStatus.ACTIVE);

        Project saved = projectRepository.save(project);

        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Project activated."
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponseDTO complete(
            Long projectId,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        companyService.ensureVerifiedMainContractor(
                contractorCompanyId
        );

        Project project = getOwnedProjectEntity(
                projectId,
                contractorCompanyId
        );

        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Only an active project can be completed."
            );
        }

        boolean unfinishedPackageExists = workPackageRepository
                .findByProjectIdAndActiveTrueOrderByCreatedAtDesc(projectId)
                .stream()
                .anyMatch(workPackage ->
                        workPackage.getStatus() != WorkPackageStatus.AWARDED
                                && workPackage.getStatus() != WorkPackageStatus.CANCELLED
                );

        if (unfinishedPackageExists) {
            throw new BusinessRuleException(
                    "Close or award all work packages before completing the project."
            );
        }

        ProjectStatus oldStatus = project.getStatus();
        project.setStatus(ProjectStatus.COMPLETED);

        Project saved = projectRepository.save(project);

        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Project completed."
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponseDTO cancel(
            Long projectId,
            String reason,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        companyService.ensureVerifiedMainContractor(
                contractorCompanyId
        );

        Project project = getOwnedProjectEntity(
                projectId,
                contractorCompanyId
        );

        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "A completed project cannot be cancelled."
            );
        }

        if (project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "The project is already cancelled."
            );
        }

        boolean nonCancellablePackageExists = workPackageRepository
                .findByProjectIdAndActiveTrueOrderByCreatedAtDesc(projectId)
                .stream()
                .anyMatch(workPackage ->
                        workPackage.getStatus() != WorkPackageStatus.DRAFT
                                && workPackage.getStatus() != WorkPackageStatus.CANCELLED
                );

        if (nonCancellablePackageExists) {
            throw new BusinessRuleException(
                    "Cancel the active procurement workflow before cancelling the project."
            );
        }

        ProjectStatus oldStatus = project.getStatus();
        project.setStatus(ProjectStatus.CANCELLED);

        Project saved = projectRepository.save(project);

        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                reason == null || reason.isBlank()
                        ? "Project cancelled."
                        : reason.trim()
        );

        return toResponse(saved);
    }

    @Override
    public ProjectResponseDTO getById(
            Long projectId,
            Long currentCompanyId,
            Role currentRole
    ) {
        Project project = getProject(projectId);

        boolean admin = currentRole == Role.ADMIN;
        boolean owner = project.getContractorCompanyId() != null
                && project.getContractorCompanyId().equals(currentCompanyId);

        if (!admin && !owner) {
            throw new UnauthorizedOperationException(
                    "You cannot view another contractor's project."
            );
        }

        return toResponse(project);
    }

    @Override
    public Page<ProjectSummaryResponseDTO> getMyProjects(
            Long contractorCompanyId,
            Pageable pageable
    ) {
        List<ProjectSummaryResponseDTO> all = projectRepository.findAll()
                .stream()
                .filter(project ->
                        Boolean.TRUE.equals(project.getActive())
                                && contractorCompanyId.equals(
                                project.getContractorCompanyId()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                Project::getCreatedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .map(this::toSummary)
                .toList();

        int start = Math.min(
                (int) pageable.getOffset(),
                all.size()
        );

        int end = Math.min(
                start + pageable.getPageSize(),
                all.size()
        );

        return new PageImpl<>(
                all.subList(start, end),
                pageable,
                all.size()
        );
    }

    @Override
    public Project getOwnedProjectEntity(
            Long projectId,
            Long contractorCompanyId
    ) {
        Project project = getProject(projectId);

        if (project.getContractorCompanyId() == null
                || !contractorCompanyId.equals(
                project.getContractorCompanyId()
        )) {
            throw new UnauthorizedOperationException(
                    "You do not own this project."
            );
        }

        return project;
    }

    private ProjectData readProjectData(ProjectRequestDTO request) {
        String title = required(
                request.getTitle(),
                "Project title"
        );

        String sector = required(
                request.getSector(),
                "Project sector"
        ).toUpperCase();

        try {
            ProjectSector.valueOf(sector);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException(
                    "Invalid project sector: " + sector
            );
        }

        String location = required(
                request.getLocation(),
                "Project location"
        );

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null || endDate == null) {
            throw new BusinessRuleException(
                    "Project start and end dates are required."
            );
        }

        return new ProjectData(
                title,
                sector,
                location,
                trimToNull(request.getDescription()),
                startDate,
                endDate
        );
    }

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException(
                    "Project end date cannot be before the start date."
            );
        }
    }

    private void ensureTitleAvailable(
            Long contractorCompanyId,
            String title,
            Long excludedProjectId
    ) {
        boolean duplicate = projectRepository.findAll()
                .stream()
                .anyMatch(project ->
                        Boolean.TRUE.equals(project.getActive())
                                && (excludedProjectId == null
                                || !excludedProjectId.equals(project.getId()))
                                && contractorCompanyId.equals(
                                project.getContractorCompanyId()
                        )
                                && project.getTitle() != null
                                && project.getTitle().equalsIgnoreCase(title)
                );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "A project with this title already exists for the contractor."
            );
        }
    }

    private void recordHistory(
            Long projectId,
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
        history.setEntityType(HistoryEntityType.PROJECT);
        history.setEntityId(projectId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(user);
        history.setNote(note);

        statusHistoryRepository.save(history);
    }

    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .filter(company -> Boolean.TRUE.equals(company.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + companyId
                ));
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(project -> Boolean.TRUE.equals(project.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId
                ));
    }

    private ProjectResponseDTO toResponse(Project project) {
        Company company = getCompany(
                project.getContractorCompanyId()
        );

        ProjectResponseDTO response = new ProjectResponseDTO();
        response.setId(project.getId());
        response.setTitle(project.getTitle());
        response.setReferenceNumber(project.getReferenceNumber());
        response.setSector(project.getSector());
        response.setLocation(project.getLocation());
        response.setDescription(project.getDescription());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setStatus(project.getStatus());
        response.setContractorCompanyId(
                project.getContractorCompanyId()
        );
        response.setContractor(
                CompanySummaryResponseDTO.fromEntity(company)
        );
        response.setWorkPackageCount(
                workPackageRepository
                        .findByProjectIdAndActiveTrueOrderByCreatedAtDesc(
                                project.getId()
                        )
                        .size()
        );
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }

    private ProjectSummaryResponseDTO toSummary(Project project) {
        ProjectSummaryResponseDTO summary = new ProjectSummaryResponseDTO();
        summary.setId(project.getId());
        summary.setTitle(project.getTitle());
        summary.setReferenceNumber(project.getReferenceNumber());
        summary.setSector(project.getSector());
        summary.setLocation(project.getLocation());
        summary.setStartDate(project.getStartDate());
        summary.setEndDate(project.getEndDate());
        summary.setStatus(project.getStatus());
        summary.setWorkPackageCount(
                workPackageRepository
                        .findByProjectIdAndActiveTrueOrderByCreatedAtDesc(
                                project.getId()
                        )
                        .size()
        );
        summary.setCreatedAt(project.getCreatedAt());
        return summary;
    }

    private String generateReference() {
        return "PRJ-"
                + LocalDateTime.now().getYear()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private String required(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(
                    fieldName + " is required."
            );
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ProjectData(
            String title,
            String sector,
            String location,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
