package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.MilestoneProgressRequestDTO;
import com.hissah.DTO.Request.MilestoneRequestDTO;
import com.hissah.DTO.Response.MilestoneResponseDTO;
import com.hissah.Entities.Award;
import com.hissah.Entities.Company;
import com.hissah.Entities.Milestone;
import com.hissah.Entities.StatusHistory;
import com.hissah.Entities.User;
import com.hissah.Enums.AwardStatus;
import com.hissah.Enums.HistoryEntityType;
import com.hissah.Enums.MilestoneStatus;
import com.hissah.Enums.NotificationType;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.AwardRepository;
import com.hissah.Repositories.MilestoneRepository;
import com.hissah.Repositories.StatusHistoryRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Services.MilestoneService;
import com.hissah.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final AwardRepository awardRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public MilestoneResponseDTO create(
            MilestoneRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        Award award = getAward(request.getAwardId());
        validateContractorOwnership(award, contractorCompanyId);
        validateActiveAward(award);
        validateDueDate(request.getDueDate());

        Milestone milestone = new Milestone();
        milestone.setAward(award);
        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(
                trimToNull(request.getDescription())
        );
        milestone.setDueDate(request.getDueDate());
        milestone.setCompletionPercent(0);
        milestone.setStatus(MilestoneStatus.NOT_STARTED);

        Milestone saved = milestoneRepository.save(milestone);
        award.addMilestone(saved);

        recordHistory(
                HistoryEntityType.MILESTONE,
                saved.getId(),
                null,
                saved.getStatus().name(),
                currentUserId,
                "Milestone created by the main contractor."
        );
        notifySubcontractor(
                award,
                "New milestone assigned",
                "A new milestone, "
                        + saved.getTitle()
                        + ", was added to award "
                        + award.getReferenceNumber()
                        + "."
        );
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MilestoneResponseDTO update(
            Long milestoneId,
            MilestoneRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        Milestone milestone = getMilestone(milestoneId);
        Award award = milestone.getAward();
        validateContractorOwnership(award, contractorCompanyId);
        validateActiveAward(award);

        if (!award.getId().equals(request.getAwardId())) {
            throw new BusinessRuleException(
                    "A milestone cannot be moved to another award."
            );
        }
        if (milestone.getStatus()
                != MilestoneStatus.NOT_STARTED) {
            throw new BusinessRuleException(
                    "Only a milestone that has not started can be edited."
            );
        }
        validateDueDate(request.getDueDate());

        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(
                trimToNull(request.getDescription())
        );
        milestone.setDueDate(request.getDueDate());

        Milestone saved = milestoneRepository.save(milestone);
        recordHistory(
                HistoryEntityType.MILESTONE,
                saved.getId(),
                saved.getStatus().name(),
                saved.getStatus().name(),
                currentUserId,
                "Milestone details updated."
        );
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MilestoneResponseDTO updateProgress(
            Long milestoneId,
            MilestoneProgressRequestDTO request,
            Long subcontractorCompanyId,
            Long currentUserId
    ) {
        Milestone milestone = getMilestone(milestoneId);
        Award award = milestone.getAward();
        validateSubcontractorOwnership(
                award,
                subcontractorCompanyId
        );
        validateActiveAward(award);

        if (milestone.getStatus()
                == MilestoneStatus.SUBMITTED
                || milestone.getStatus()
                == MilestoneStatus.APPROVED) {
            throw new BusinessRuleException(
                    "A submitted or approved milestone cannot be edited."
            );
        }
        if (request.getStatus()
                == MilestoneStatus.SUBMITTED
                || request.getStatus()
                == MilestoneStatus.APPROVED) {
            throw new BusinessRuleException(
                    "Use the submit action to submit progress. Approval belongs to the contractor."
            );
        }

        MilestoneStatus oldStatus = milestone.getStatus();
        milestone.setCompletionPercent(
                request.getCompletionPercent()
        );
        milestone.setEvidenceNote(
                trimToNull(request.getEvidenceNote())
        );

        MilestoneStatus requestedStatus =
                request.getStatus();

        if (milestone.getDueDate().isBefore(LocalDate.now())
                && request.getCompletionPercent() < 100) {
            requestedStatus = MilestoneStatus.OVERDUE;
        } else if (request.getCompletionPercent() > 0
                && requestedStatus
                == MilestoneStatus.NOT_STARTED) {
            requestedStatus = MilestoneStatus.IN_PROGRESS;
        }

        milestone.setStatus(requestedStatus);
        Milestone saved = milestoneRepository.save(milestone);

        recordHistory(
                HistoryEntityType.MILESTONE,
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Milestone progress updated to "
                        + saved.getCompletionPercent()
                        + "%."
        );
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MilestoneResponseDTO submit(
            Long milestoneId,
            Long subcontractorCompanyId,
            Long currentUserId
    ) {
        Milestone milestone = getMilestone(milestoneId);
        Award award = milestone.getAward();
        validateSubcontractorOwnership(
                award,
                subcontractorCompanyId
        );
        validateActiveAward(award);

        if (milestone.getStatus()
                == MilestoneStatus.APPROVED) {
            throw new BusinessRuleException(
                    "The milestone is already approved."
            );
        }
        if (milestone.getStatus()
                == MilestoneStatus.SUBMITTED) {
            throw new BusinessRuleException(
                    "The milestone is already submitted."
            );
        }
        if (!Integer.valueOf(100).equals(
                milestone.getCompletionPercent())) {
            throw new BusinessRuleException(
                    "A milestone must reach 100% before submission."
            );
        }
        if (milestone.getEvidenceNote() == null
                || milestone.getEvidenceNote().isBlank()) {
            throw new BusinessRuleException(
                    "Completion evidence or a completion note is required."
            );
        }

        MilestoneStatus oldStatus = milestone.getStatus();
        milestone.setStatus(MilestoneStatus.SUBMITTED);
        milestone.setSubmittedAt(LocalDateTime.now());

        Milestone saved = milestoneRepository.save(milestone);
        recordHistory(
                HistoryEntityType.MILESTONE,
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Milestone submitted for contractor approval."
        );
        notifyContractor(
                award,
                "Milestone submitted",
                saved.getTitle()
                        + " was submitted for your approval."
        );
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MilestoneResponseDTO approve(
            Long milestoneId,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        Milestone milestone = getMilestone(milestoneId);
        Award award = milestone.getAward();
        validateContractorOwnership(award, contractorCompanyId);
        validateActiveAward(award);

        if (milestone.getStatus()
                != MilestoneStatus.SUBMITTED) {
            throw new BusinessRuleException(
                    "Only a submitted milestone can be approved."
            );
        }

        MilestoneStatus oldStatus = milestone.getStatus();
        milestone.setStatus(MilestoneStatus.APPROVED);
        milestone.setApprovedAt(LocalDateTime.now());
        milestone.setCompletionPercent(100);

        Milestone saved =
                milestoneRepository.saveAndFlush(milestone);

        recordHistory(
                HistoryEntityType.MILESTONE,
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Milestone approved by the main contractor."
        );
        notifySubcontractor(
                award,
                "Milestone approved",
                saved.getTitle()
                        + " was approved by the main contractor."
        );

        completeAwardWhenAllMilestonesApproved(
                award,
                currentUserId
        );
        return toResponse(saved);
    }

    @Override
    public List<MilestoneResponseDTO> getByAward(
            Long awardId,
            Long currentCompanyId
    ) {
        Award award = getAward(awardId);
        validateParticipantVisibility(
                award,
                currentCompanyId
        );

        return milestoneRepository
                .findByAwardIdAndActiveTrueOrderByDueDateAsc(
                        awardId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void completeAwardWhenAllMilestonesApproved(
            Award award,
            Long currentUserId
    ) {
        boolean unfinishedExists =
                milestoneRepository
                        .existsByAwardIdAndStatusNotAndActiveTrue(
                                award.getId(),
                                MilestoneStatus.APPROVED
                        );

        if (!unfinishedExists
                && award.getStatus() == AwardStatus.ACTIVE) {
            AwardStatus oldStatus = award.getStatus();
            award.setStatus(AwardStatus.COMPLETED);
            awardRepository.save(award);

            recordHistory(
                    HistoryEntityType.AWARD,
                    award.getId(),
                    oldStatus.name(),
                    award.getStatus().name(),
                    currentUserId,
                    "All milestones approved. Award completed."
            );

            notifyContractor(
                    award,
                    "Award completed",
                    "All milestones for award "
                            + award.getReferenceNumber()
                            + " are approved."
            );
            notifySubcontractor(
                    award,
                    "Award completed",
                    "All milestones for award "
                            + award.getReferenceNumber()
                            + " are approved."
            );
        }
    }

    private Milestone getMilestone(Long milestoneId) {
        return milestoneRepository
                .findByIdAndActiveTrue(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Milestone not found with id: "
                                + milestoneId
                ));
    }

    private Award getAward(Long awardId) {
        return awardRepository
                .findByIdAndActiveTrue(awardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Award not found with id: " + awardId
                ));
    }

    private void validateActiveAward(Award award) {
        if (award.getStatus() != AwardStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Milestones can only be changed for an active award."
            );
        }
    }

    private void validateDueDate(LocalDate dueDate) {
        if (dueDate == null
                || dueDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException(
                    "Milestone due date cannot be in the past."
            );
        }
    }

    private void validateContractorOwnership(
            Award award,
            Long contractorCompanyId
    ) {
        Long ownerId =
                award.getWorkPackage()
                        .getProject()
                        .getContractorCompany()
                        .getId();

        if (!ownerId.equals(contractorCompanyId)) {
            throw new UnauthorizedOperationException(
                    "Only the owning main contractor can perform this action."
            );
        }
    }

    private void validateSubcontractorOwnership(
            Award award,
            Long subcontractorCompanyId
    ) {
        Long selectedCompanyId =
                award.getBid()
                        .getBidderCompany()
                        .getId();

        if (!selectedCompanyId.equals(
                subcontractorCompanyId)) {
            throw new UnauthorizedOperationException(
                    "Only the selected subcontractor can update this milestone."
            );
        }
    }

    private void validateParticipantVisibility(
            Award award,
            Long currentCompanyId
    ) {
        Long contractorId =
                award.getWorkPackage()
                        .getProject()
                        .getContractorCompany()
                        .getId();

        Long subcontractorId =
                award.getBid()
                        .getBidderCompany()
                        .getId();

        if (!currentCompanyId.equals(contractorId)
                && !currentCompanyId.equals(
                subcontractorId)) {
            throw new UnauthorizedOperationException(
                    "You cannot view these milestones."
            );
        }
    }

    private void notifyContractor(
            Award award,
            String title,
            String message
    ) {
        Company contractor =
                award.getWorkPackage()
                        .getProject()
                        .getContractorCompany();

        if (contractor.getUser() != null) {
            notificationService.create(
                    contractor.getUser().getId(),
                    title,
                    message,
                    NotificationType.MILESTONE
            );
        }
    }

    private void notifySubcontractor(
            Award award,
            String title,
            String message
    ) {
        Company subcontractor =
                award.getBid().getBidderCompany();

        if (subcontractor.getUser() != null) {
            notificationService.create(
                    subcontractor.getUser().getId(),
                    title,
                    message,
                    NotificationType.MILESTONE
            );
        }
    }

    private void recordHistory(
            HistoryEntityType entityType,
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
        history.setEntityType(entityType);
        history.setEntityId(entityId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(user);
        history.setNote(note);

        statusHistoryRepository.save(history);
    }

    private MilestoneResponseDTO toResponse(
            Milestone milestone
    ) {
        boolean overdue =
                milestone.getDueDate() != null
                        && milestone.getDueDate()
                        .isBefore(LocalDate.now())
                        && milestone.getStatus()
                        != MilestoneStatus.APPROVED;

        return MilestoneResponseDTO.builder()
                .id(milestone.getId())
                .awardId(milestone.getAward().getId())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .dueDate(milestone.getDueDate())
                .completionPercent(
                        milestone.getCompletionPercent()
                )
                .status(milestone.getStatus())
                .evidenceNote(milestone.getEvidenceNote())
                .submittedAt(milestone.getSubmittedAt())
                .approvedAt(milestone.getApprovedAt())
                .overdue(overdue)
                .createdAt(milestone.getCreatedAt())
                .updatedAt(milestone.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
