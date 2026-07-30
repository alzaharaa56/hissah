package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.AwardRequestDTO;
import com.hissah.DTO.Response.AwardResponseDTO;
import com.hissah.DTO.Response.MilestoneResponseDTO;
import com.hissah.Entities.Award;
import com.hissah.Entities.Bid;
import com.hissah.Entities.Company;
import com.hissah.Entities.Milestone;
import com.hissah.Entities.StatusHistory;
import com.hissah.Entities.User;
import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.AwardStatus;
import com.hissah.Enums.BidStatus;
import com.hissah.Enums.HistoryEntityType;
import com.hissah.Enums.MilestoneStatus;
import com.hissah.Enums.NotificationType;
import com.hissah.Enums.WorkPackageStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.AwardRepository;
import com.hissah.Repositories.BidRepository;
import com.hissah.Repositories.MilestoneRepository;
import com.hissah.Repositories.StatusHistoryRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Repositories.WorkPackageRepository;
import com.hissah.Services.AwardService;
import com.hissah.Services.NotificationService;
import com.hissah.Services.Implementations.Support.CompanyReferenceSupport;
import com.hissah.Utilities.OwnershipValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AwardServiceImpl implements AwardService {

    private static final Set<BidStatus> AWARDABLE_BID_STATUSES =
            Set.of(BidStatus.SUBMITTED, BidStatus.SHORTLISTED);

    private static final Set<BidStatus> COMPETING_BID_STATUSES =
            Set.of(BidStatus.SUBMITTED, BidStatus.SHORTLISTED);

    private final AwardRepository awardRepository;
    private final BidRepository bidRepository;
    private final WorkPackageRepository workPackageRepository;
    private final MilestoneRepository milestoneRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OwnershipValidator ownershipValidator;
    private final CompanyReferenceSupport companyReferenceSupport;

    @Override
    @Transactional
    public AwardResponseDTO awardBid(
            AwardRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        Bid selectedBid = bidRepository.findByIdForUpdate(request.getBidId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Selected bid not found with id: " + request.getBidId()
                ));

        WorkPackage workPackage =
                workPackageRepository.findByIdForUpdate(
                                selectedBid.getWorkPackage().getId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Work package not found for the selected bid."
                        ));

        ownershipValidator.validateWorkPackageOwnership(
                contractorCompanyId,
                workPackage
        );
        validateAwardable(workPackage, selectedBid);

        User currentUser = getUser(currentUserId);
        LocalDateTime now = LocalDateTime.now();

        List<Bid> competingBids =
                new ArrayList<>(
                        bidRepository.findByWorkPackageIdAndStatusInAndActiveTrue(
                                workPackage.getId(),
                                COMPETING_BID_STATUSES
                        )
                );

        BidStatus selectedOldStatus = selectedBid.getStatus();
        selectedBid.setStatus(BidStatus.AWARDED);
        selectedBid.setDecisionReason("Selected for award.");
        bidRepository.save(selectedBid);

        for (Bid competitor : competingBids) {
            if (competitor.getId().equals(selectedBid.getId())) {
                continue;
            }

            BidStatus oldStatus = competitor.getStatus();
            competitor.setStatus(BidStatus.REJECTED);
            competitor.setDecisionReason(
                    "Another bid was selected for this work package."
            );
            bidRepository.save(competitor);

            recordHistory(
                    HistoryEntityType.BID,
                    competitor.getId(),
                    oldStatus.name(),
                    competitor.getStatus().name(),
                    currentUser,
                    "Bid automatically rejected after award selection."
            );
            notifyRejectedBidder(competitor);
        }

        WorkPackageStatus packageOldStatus = workPackage.getStatus();
        workPackage.setStatus(WorkPackageStatus.AWARDED);
        workPackage.setClosedAt(now);
        workPackageRepository.save(workPackage);

        Award award = new Award();
        award.setReferenceNumber(generateReference("AWR"));
        award.setAwardedAt(now);
        award.setAgreedAmount(request.getAgreedAmount());
        award.setAgreedDeliveryDays(request.getAgreedDeliveryDays());
        award.setNotes(trimToNull(request.getNotes()));
        award.setStatus(AwardStatus.ACTIVE);
        award.setBid(selectedBid);
        award.setWorkPackage(workPackage);

        Award savedAward = awardRepository.save(award);
        selectedBid.setAward(savedAward);
        workPackage.setAward(savedAward);

        recordHistory(
                HistoryEntityType.BID,
                selectedBid.getId(),
                selectedOldStatus.name(),
                selectedBid.getStatus().name(),
                currentUser,
                "Bid selected and awarded."
        );
        recordHistory(
                HistoryEntityType.WORK_PACKAGE,
                workPackage.getId(),
                packageOldStatus.name(),
                workPackage.getStatus().name(),
                currentUser,
                "Work package awarded to "
                        + companyName(selectedBid.getBidderCompany())
                        + "."
        );
        recordHistory(
                HistoryEntityType.AWARD,
                savedAward.getId(),
                null,
                savedAward.getStatus().name(),
                currentUser,
                "Award record created."
        );

        notifyAwardWinner(savedAward);
        return toResponse(savedAward);
    }

    @Override
    public AwardResponseDTO getById(
            Long awardId,
            Long currentCompanyId
    ) {
        Award award = getAward(awardId);
        validateAwardVisibility(award, currentCompanyId);
        return toResponse(award);
    }

    @Override
    public AwardResponseDTO getByWorkPackage(
            Long workPackageId,
            Long currentCompanyId
    ) {
        Award award =
                awardRepository.findByWorkPackageIdAndActiveTrue(
                                workPackageId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No award exists for work package id: "
                                        + workPackageId
                        ));

        validateAwardVisibility(award, currentCompanyId);
        return toResponse(award);
    }

    @Override
    public List<AwardResponseDTO> getMyAwards(
            Long currentCompanyId
    ) {
        Map<Long, Award> uniqueAwards = new LinkedHashMap<>();

        awardRepository
                .findByBidBidderCompanyIdAndActiveTrueOrderByAwardedAtDesc(
                        currentCompanyId
                )
                .forEach(award ->
                        uniqueAwards.put(award.getId(), award)
                );

        awardRepository
                .findByWorkPackageProjectContractorCompanyIdAndActiveTrueOrderByAwardedAtDesc(
                        currentCompanyId
                )
                .forEach(award ->
                        uniqueAwards.put(award.getId(), award)
                );

        return uniqueAwards.values()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateAwardable(
            WorkPackage workPackage,
            Bid selectedBid
    ) {
        if (!selectedBid.getWorkPackage().getId()
                .equals(workPackage.getId())) {
            throw new BusinessRuleException(
                    "The selected bid does not belong to the work package."
            );
        }
        if (!AWARDABLE_BID_STATUSES.contains(
                selectedBid.getStatus())) {
            throw new BusinessRuleException(
                    "Only submitted or shortlisted bids can be awarded."
            );
        }
        if (awardRepository.existsByWorkPackageIdAndActiveTrue(
                workPackage.getId())) {
            throw new BusinessRuleException(
                    "This work package already has an award."
            );
        }
        if (workPackage.getStatus() == WorkPackageStatus.AWARDED
                || workPackage.getStatus()
                == WorkPackageStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "This work package cannot be awarded in its current status."
            );
        }

        boolean biddingStillOpen =
                workPackage.getStatus() == WorkPackageStatus.OPEN
                        && workPackage.getDeadline() != null
                        && workPackage.getDeadline()
                        .isAfter(LocalDateTime.now());

        if (biddingStillOpen) {
            throw new BusinessRuleException(
                    "Close the bidding period before selecting an award."
            );
        }
    }

    private Award getAward(Long awardId) {
        return awardRepository.findByIdAndActiveTrue(awardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Award not found with id: " + awardId
                ));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));
    }

    private void validateAwardVisibility(
            Award award,
            Long currentCompanyId
    ) {
        Long contractorCompanyId =
                award.getWorkPackage()
                        .getProject()
                        .getContractorCompanyId();

        Long subcontractorCompanyId =
                award.getBid()
                        .getBidderCompany()
                        .getId();

        if (!currentCompanyId.equals(contractorCompanyId)
                && !currentCompanyId.equals(
                subcontractorCompanyId)) {
            throw new UnauthorizedOperationException(
                    "You cannot view this award."
            );
        }
    }

    private void notifyAwardWinner(Award award) {
        Company winner = award.getBid().getBidderCompany();

        if (winner.getUserId() != null) {
            notificationService.create(
                    winner.getUserId(),
                    "Bid awarded",
                    "Congratulations. Your bid "
                            + award.getBid().getReferenceNumber()
                            + " was awarded for "
                            + award.getWorkPackage().getTitle()
                            + ".",
                    NotificationType.AWARD
            );
        }
    }

    private void notifyRejectedBidder(Bid bid) {
        Company bidder = bid.getBidderCompany();

        if (bidder.getUserId() != null) {
            notificationService.create(
                    bidder.getUserId(),
                    "Bid result",
                    "Your bid "
                            + bid.getReferenceNumber()
                            + " was not selected for "
                            + bid.getWorkPackage().getTitle()
                            + ".",
                    NotificationType.BID_STATUS
            );
        }
    }

    private void recordHistory(
            HistoryEntityType entityType,
            Long entityId,
            String oldStatus,
            String newStatus,
            User changedBy,
            String note
    ) {
        StatusHistory history = new StatusHistory();
        history.setEntityType(entityType);
        history.setEntityId(entityId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);
        statusHistoryRepository.save(history);
    }

    private AwardResponseDTO toResponse(Award award) {
        Bid bid = award.getBid();
        WorkPackage workPackage = award.getWorkPackage();
        Company subcontractor = bid.getBidderCompany();
        Company contractor =
                companyReferenceSupport.requireProjectContractor(workPackage.getProject());

        List<MilestoneResponseDTO> milestones =
                milestoneRepository
                        .findByAwardIdAndActiveTrueOrderByDueDateAsc(
                                award.getId()
                        )
                        .stream()
                        .map(this::toMilestoneResponse)
                        .toList();

        return AwardResponseDTO.builder()
                .id(award.getId())
                .referenceNumber(award.getReferenceNumber())
                .awardedAt(award.getAwardedAt())
                .agreedAmount(award.getAgreedAmount())
                .agreedDeliveryDays(
                        award.getAgreedDeliveryDays()
                )
                .notes(award.getNotes())
                .status(award.getStatus())
                .bidId(bid.getId())
                .bidReferenceNumber(bid.getReferenceNumber())
                .workPackageId(workPackage.getId())
                .workPackageReferenceNumber(
                        workPackage.getReferenceNumber()
                )
                .workPackageTitle(workPackage.getTitle())
                .subcontractorCompanyId(subcontractor.getId())
                .subcontractorCompanyName(
                        companyName(subcontractor)
                )
                .contractorCompanyId(contractor.getId())
                .contractorCompanyName(companyName(contractor))
                .milestones(milestones)
                .createdAt(award.getCreatedAt())
                .updatedAt(award.getUpdatedAt())
                .build();
    }

    private MilestoneResponseDTO toMilestoneResponse(
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

    private String companyName(Company company) {
        if (company.getTradingName() != null
                && !company.getTradingName().isBlank()) {
            return company.getTradingName();
        }
        return company.getLegalName();
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
