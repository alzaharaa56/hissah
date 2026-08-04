package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.BidDecisionRequestDTO;
import com.hissah.DTO.Request.BidRequestDTO;
import com.hissah.DTO.Response.BidComparisonResponseDTO;
import com.hissah.DTO.Response.BidDocumentDownloadDTO;
import com.hissah.DTO.Response.BidResponseDTO;
import com.hissah.Entities.Bid;
import com.hissah.Entities.BidDocument;
import com.hissah.Entities.Company;
import com.hissah.Entities.CompanyDocument;
import com.hissah.Entities.StatusHistory;
import com.hissah.Entities.User;
import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.BidStatus;
import com.hissah.Enums.CompanyType;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.EligibilityType;
import com.hissah.Enums.HistoryEntityType;
import com.hissah.Enums.NotificationType;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Enums.WorkPackageStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.BidDocumentRepository;
import com.hissah.Repositories.BidRepository;
import com.hissah.Repositories.CompanyDocumentRepository;
import com.hissah.Repositories.StatusHistoryRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Repositories.WorkPackageRepository;
import com.hissah.Services.BidService;
import com.hissah.Services.NotificationService;
import com.hissah.Services.Implementations.Support.BidDocumentStorageService;
import com.hissah.Services.Implementations.Support.BidResponseMapper;
import com.hissah.Services.Implementations.Support.CompanyReferenceSupport;
import com.hissah.Utilities.OwnershipValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidServiceImpl implements BidService {

    private static final Set<BidStatus> ACTIVE_BID_STATUSES = EnumSet.of(
            BidStatus.DRAFT,
            BidStatus.SUBMITTED,
            BidStatus.SHORTLISTED,
            BidStatus.AWARDED
    );

    private static final Set<BidStatus> COMPARISON_STATUSES = EnumSet.of(
            BidStatus.SUBMITTED,
            BidStatus.SHORTLISTED,
            BidStatus.REJECTED,
            BidStatus.AWARDED
    );

    private final BidRepository bidRepository;
    private final BidDocumentRepository bidDocumentRepository;
    private final WorkPackageRepository workPackageRepository;
    private final CompanyDocumentRepository companyDocumentRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OwnershipValidator ownershipValidator;
    private final CompanyReferenceSupport companyReferenceSupport;
    private final BidDocumentStorageService documentStorageService;
    private final BidResponseMapper bidResponseMapper;

    @Override
    @Transactional
    public BidResponseDTO createDraft(
            BidRequestDTO request,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = createBid(request, currentCompanyId, BidStatus.DRAFT);
        Bid saved = bidRepository.save(bid);
        documentStorageService.store(saved, request);
        recordHistory(saved, null, currentUserId, "Bid created as draft.");
        return bidResponseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BidResponseDTO submitNew(
            BidRequestDTO request,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = createBid(request, currentCompanyId, BidStatus.SUBMITTED);
        bid.setSubmittedAt(LocalDateTime.now());

        Bid saved = bidRepository.save(bid);
        documentStorageService.store(saved, request);
        recordHistory(saved, null, currentUserId, "Bid submitted.");
        notifyContractor(saved, "New bid received",
                "A new bid was submitted for " + saved.getWorkPackage().getTitle() + ".");
        return bidResponseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BidResponseDTO update(
            Long bidId,
            BidRequestDTO request,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = getOwnedBid(bidId, currentCompanyId, true);

        if (bid.getStatus() != BidStatus.DRAFT) {
            throw new BusinessRuleException("Only a draft bid can be edited.");
        }
        if (!bid.getWorkPackage().getId().equals(request.getWorkPackageId())) {
            throw new BusinessRuleException(
                    "A bid cannot be moved to another work package."
            );
        }

        validateOpenPackage(bid.getWorkPackage());
        applyRequest(bid, request);
        Bid saved = bidRepository.save(bid);
        documentStorageService.store(saved, request);
        recordHistory(
                saved,
                BidStatus.DRAFT,
                currentUserId,
                "Draft bid details updated."
        );
        return bidResponseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BidResponseDTO submitDraft(
            Long bidId,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = getOwnedBid(bidId, currentCompanyId, true);

        if (bid.getStatus() != BidStatus.DRAFT) {
            throw new BusinessRuleException("Only a draft bid can be submitted.");
        }
        validateOpenPackage(bid.getWorkPackage());

        BidStatus oldStatus = bid.getStatus();
        bid.setStatus(BidStatus.SUBMITTED);
        bid.setSubmittedAt(LocalDateTime.now());

        Bid saved = bidRepository.save(bid);
        recordHistory(saved, oldStatus, currentUserId, "Draft bid submitted.");
        notifyContractor(saved, "New bid received",
                "A bid was submitted for " + saved.getWorkPackage().getTitle() + ".");
        return bidResponseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BidResponseDTO withdraw(
            Long bidId,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = getOwnedBid(bidId, currentCompanyId, true);

        if (!EnumSet.of(
                BidStatus.DRAFT,
                BidStatus.SUBMITTED,
                BidStatus.SHORTLISTED
        ).contains(bid.getStatus())) {
            throw new BusinessRuleException(
                    "Only draft, submitted, or shortlisted bids can be withdrawn."
            );
        }
        if (bid.getWorkPackage().getStatus() == WorkPackageStatus.AWARDED) {
            throw new BusinessRuleException(
                    "A bid cannot be withdrawn after the package is awarded."
            );
        }

        BidStatus oldStatus = bid.getStatus();
        bid.setStatus(BidStatus.WITHDRAWN);
        bid.setDecisionReason("Withdrawn by the bidder.");

        Bid saved = bidRepository.save(bid);
        recordHistory(saved, oldStatus, currentUserId, "Bid withdrawn by the SME.");
        notifyContractor(saved, "Bid withdrawn",
                companyReferenceSupport.companyName(saved.getBidderCompany())
                        + " withdrew its bid for "
                        + saved.getWorkPackage().getTitle() + ".");
        return bidResponseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BidResponseDTO decide(
            Long bidId,
            BidDecisionRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    ) {
        Bid bid = bidRepository.findByIdForUpdate(bidId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bid not found with id: " + bidId
                ));

        ownershipValidator.validateWorkPackageOwnership(
                contractorCompanyId,
                bid.getWorkPackage()
        );
        validateDecision(request.getDecision(), bid.getStatus());

        BidStatus oldStatus = bid.getStatus();
        bid.setStatus(request.getDecision());
        bid.setDecisionReason(trimToNull(request.getReason()));

        Bid saved = bidRepository.save(bid);
        recordHistory(
                saved,
                oldStatus,
                currentUserId,
                request.getReason() == null || request.getReason().isBlank()
                        ? "Bid evaluation decision recorded."
                        : request.getReason().trim()
        );
        notifyBidder(saved, "Bid status updated",
                "Your bid " + saved.getReferenceNumber()
                        + " is now " + saved.getStatus().name() + ".");
        return bidResponseMapper.toResponse(saved);
    }

    @Override
    public BidResponseDTO getById(Long bidId, Long currentCompanyId) {
        Bid bid = getBid(bidId);
        validateVisibility(bid, currentCompanyId);
        return bidResponseMapper.toResponse(bid);
    }

    @Override
    public Page<BidResponseDTO> getMyBids(
            Long currentCompanyId,
            Pageable pageable
    ) {
        return bidRepository
                .findByBidderCompanyIdAndActiveTrueOrderByCreatedAtDesc(
                        currentCompanyId,
                        pageable
                )
                .map(bidResponseMapper::toResponse);
    }

    @Override
    public List<BidComparisonResponseDTO> compareForWorkPackage(
            Long workPackageId,
            Long contractorCompanyId
    ) {
        WorkPackage workPackage = getWorkPackage(workPackageId);
        ownershipValidator.validateWorkPackageOwnership(
                contractorCompanyId,
                workPackage
        );

        AtomicLong rank = new AtomicLong(1);
        return bidRepository
                .findByWorkPackageIdAndStatusInAndActiveTrue(
                        workPackageId,
                        COMPARISON_STATUSES
                )
                .stream()
                .sorted(Comparator
                        .comparing(Bid::getAmount)
                        .thenComparing(Bid::getDeliveryDays))
                .map(bid -> bidResponseMapper.toComparison(bid, rank.getAndIncrement()))
                .toList();
    }

    @Override
    public BidDocumentDownloadDTO downloadDocument(
            Long documentId,
            Long currentCompanyId
    ) {
        BidDocument document = documentStorageService.getDocument(documentId);
        validateVisibility(document.getBid(), currentCompanyId);
        return documentStorageService.prepareDownload(documentId);
    }

    private Bid createBid(
            BidRequestDTO request,
            Long companyId,
            BidStatus initialStatus
    ) {
        Company bidder = companyReferenceSupport.requireCompany(companyId);
        WorkPackage workPackage = getWorkPackage(request.getWorkPackageId());

        validateBidder(bidder);
        validateOpenPackage(workPackage);
        validateNotOwnPackage(bidder, workPackage);
        validateEligibility(bidder, workPackage);
        ensureNoDuplicateBid(companyId, workPackage.getId());

        Bid bid = new Bid();
        bid.setReferenceNumber(generateReference());
        bid.setBidderCompany(bidder);
        bid.setWorkPackage(workPackage);
        bid.setStatus(initialStatus);
        applyRequest(bid, request);
        return bid;
    }

    private void applyRequest(Bid bid, BidRequestDTO request) {
        bid.setAmount(request.getAmount());
        bid.setDeliveryDays(request.getDeliveryDays());
        bid.setProposalText(request.getProposalText().trim());
        bid.setDecisionReason(null);
    }

    private void validateBidder(Company company) {
        if (company.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BusinessRuleException(
                    "Only a verified company can submit a bid."
            );
        }
        if (company.getCompanyType() != CompanyType.SUBCONTRACTOR
                && company.getCompanyType() != CompanyType.BOTH) {
            throw new BusinessRuleException(
                    "Only a subcontractor or dual-role company can submit a bid."
            );
        }
    }

    private void validateOpenPackage(WorkPackage workPackage) {
        if (workPackage.getStatus() != WorkPackageStatus.OPEN) {
            throw new BusinessRuleException(
                    "Bids can only be submitted to an open work package."
            );
        }
        if (workPackage.getDeadline() == null
                || !workPackage.getDeadline().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "The work package bidding deadline has passed."
            );
        }
    }

    private void validateNotOwnPackage(
            Company bidder,
            WorkPackage workPackage
    ) {
        Long contractorId = workPackage.getProject().getContractorCompanyId();
        if (bidder.getId().equals(contractorId)) {
            throw new BusinessRuleException(
                    "A main contractor cannot bid on its own work package."
            );
        }
    }

    private void validateEligibility(
            Company bidder,
            WorkPackage workPackage
    ) {
        EligibilityType eligibility = workPackage.getEligibilityType();

        if (eligibility == EligibilityType.SME_ONLY
                && bidder.getCompanyType() == CompanyType.MAIN_CONTRACTOR) {
            throw new BusinessRuleException(
                    "This work package is reserved for SMEs."
            );
        }
        if (eligibility == EligibilityType.RIYADA_REQUIRED
                && !hasVerifiedRiyadaCard(bidder.getId())) {
            throw new BusinessRuleException(
                    "This work package requires a verified Riyada Card."
            );
        }
    }

    private boolean hasVerifiedRiyadaCard(Long companyId) {
        LocalDate today = LocalDate.now();

        return companyDocumentRepository.findDocumentsByCompanyId(companyId)
                .stream()
                .anyMatch(document ->
                        document.getDocumentType() == DocumentType.RIYADA_CARD
                                && document.getVerificationStatus()
                                == VerificationStatus.VERIFIED
                                && (
                                document.getExpiryDate() == null
                                        || !document.getExpiryDate().isBefore(today)
                        )
                );
    }

    private void ensureNoDuplicateBid(Long companyId, Long workPackageId) {
        boolean exists = bidRepository
                .existsByBidderCompanyIdAndWorkPackageIdAndStatusInAndActiveTrue(
                        companyId,
                        workPackageId,
                        ACTIVE_BID_STATUSES
                );

        if (exists) {
            throw new BusinessRuleException(
                    "Your company already has an active bid for this work package."
            );
        }
    }

    private void validateDecision(
            BidStatus decision,
            BidStatus currentStatus
    ) {
        if (decision != BidStatus.SHORTLISTED
                && decision != BidStatus.REJECTED) {
            throw new BusinessRuleException(
                    "The supported decisions are SHORTLISTED and REJECTED."
            );
        }
        if (currentStatus != BidStatus.SUBMITTED
                && currentStatus != BidStatus.SHORTLISTED) {
            throw new BusinessRuleException(
                    "Only submitted or shortlisted bids can be evaluated."
            );
        }
    }

    private void validateVisibility(Bid bid, Long companyId) {
        Long bidderId = bid.getBidderCompany().getId();
        Long contractorId = bid.getWorkPackage()
                .getProject()
                .getContractorCompanyId();

        if (!companyId.equals(bidderId) && !companyId.equals(contractorId)) {
            throw new UnauthorizedOperationException(
                    "You cannot view this bid."
            );
        }
    }

    private Bid getOwnedBid(Long bidId, Long companyId, boolean lock) {
        Bid bid = (lock
                ? bidRepository.findByIdForUpdate(bidId)
                : bidRepository.findByIdAndActiveTrue(bidId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bid not found with id: " + bidId
                ));

        ownershipValidator.validateBidOwnership(companyId, bid);
        return bid;
    }

    private Bid getBid(Long bidId) {
        return bidRepository.findByIdAndActiveTrue(bidId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bid not found with id: " + bidId
                ));
    }

    private WorkPackage getWorkPackage(Long workPackageId) {
        return workPackageRepository.findByIdAndActiveTrue(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work package not found with id: " + workPackageId
                ));
    }

    private void notifyContractor(
            Bid bid,
            String title,
            String message
    ) {
        Company contractor = companyReferenceSupport.requireProjectContractor(
                bid.getWorkPackage().getProject()
        );
        notifyUser(contractor.getUserId(), title, message, NotificationType.NEW_BID);
    }

    private void notifyBidder(
            Bid bid,
            String title,
            String message
    ) {
        notifyUser(
                bid.getBidderCompany().getUserId(),
                title,
                message,
                NotificationType.BID_STATUS
        );
    }

    private void notifyUser(
            Long userId,
            String title,
            String message,
            NotificationType type
    ) {
        if (userId != null) {
            notificationService.create(userId, title, message, type);
        }
    }

    private void recordHistory(
            Bid bid,
            BidStatus oldStatus,
            Long userId,
            String note
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        StatusHistory history = new StatusHistory();
        history.setEntityType(HistoryEntityType.BID);
        history.setEntityId(bid.getId());
        history.setOldStatus(oldStatus == null ? null : oldStatus.name());
        history.setNewStatus(bid.getStatus().name());
        history.setChangedBy(user);
        history.setNote(note);
        statusHistoryRepository.save(history);
    }

    private String generateReference() {
        return "BID-"
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
