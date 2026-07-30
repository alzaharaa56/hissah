package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.BidDecisionRequestDTO;
import com.hissah.DTO.Request.BidRequestDTO;
import com.hissah.DTO.Response.BidComparisonResponseDTO;
import com.hissah.DTO.Response.BidResponseDTO;
import com.hissah.DTO.Response.BidDocumentDownloadDTO;
import com.hissah.Entities.Bid;
import com.hissah.Entities.BidDocument;
import com.hissah.Entities.Company;
import com.hissah.Entities.StatusHistory;
import com.hissah.Entities.User;
import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.BidStatus;
import com.hissah.Enums.DocumentType;
import com.hissah.Enums.HistoryEntityType;
import com.hissah.Enums.NotificationType;
import com.hissah.Enums.WorkPackageStatus;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.FileStorageException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Repositories.BidDocumentRepository;
import com.hissah.Repositories.BidRepository;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.StatusHistoryRepository;
import com.hissah.Repositories.UserRepository;
import com.hissah.Repositories.WorkPackageRepository;
import com.hissah.Services.BidService;
import com.hissah.Services.NotificationService;
import com.hissah.Utilities.FileNameGenerator;
import com.hissah.Utilities.OwnershipValidator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Enumset;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidServiceImpl implements BidService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final int MAX_DOCUMENTS_PER_BID = 5;

    private static final Set<BidStatus> ACTIVE_DUPLICATE_STATUSES =
            EnumSet.of(
                    BidStatus.DRAFT,
                    BidStatus.SUBMITTED,
                    BidStatus.SHORTLISTED,
                    BidStatus.AWARDED
            );

    private static final Set<BidStatus> COMPARISON_STATUSES =
            EnumSet.of(
                    BidStatus.SUBMITTED,
                    BidStatus.SHORTLISTED,
                    BidStatus.REJECTED,
                    BidStatus.AWARDED
            );

    private final BidRepository bidRepository;
    private final BidDocumentRepository bidDocumentRepository;
    private final WorkPackageRepository workPackageRepository;
    private final CompanyRepository companyRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OwnershipValidator ownershipValidator;
    private final EntityManager entityManager;

    @Qualifier("uploadRootPath")
    private final Path uploadRootPath;

    @Override
    @Transactional
    public BidResponseDTO createDraft(
            BidRequestDTO request,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = buildNewBid(
                request,
                currentCompanyId,
                BidStatus.DRAFT
        );
        Bid saved = bidRepository.save(bid);
        storeDocuments(saved, request);

        recordHistory(
                saved.getId(),
                null,
                saved.getStatus().name(),
                currentUserId,
                "Bid created as draft."
        );
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BidResponseDTO submitNew(
            BidRequestDTO request,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = buildNewBid(
                request,
                currentCompanyId,
                BidStatus.SUBMITTED
        );
        bid.setSubmittedAt(LocalDateTime.now());

        Bid saved = bidRepository.save(bid);
        storeDocuments(saved, request);

        recordHistory(
                saved.getId(),
                null,
                saved.getStatus().name(),
                currentUserId,
                "Bid submitted to the main contractor."
        );
        notifyContractorAboutNewBid(saved);
        return toResponse(saved);
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
            throw new BusinessRuleException(
                    "Only a draft bid can be edited."
            );
        }
        if (!bid.getWorkPackage().getId().equals(request.getWorkPackageId())) {
            throw new BusinessRuleException(
                    "A bid cannot be moved to a different work package."
            );
        }

        validatePackageOpenForBidding(bid.getWorkPackage());
        applyRequest(bid, request);

        Bid saved = bidRepository.save(bid);
        storeDocuments(saved, request);

        recordHistory(
                saved.getId(),
                BidStatus.DRAFT.name(),
                BidStatus.DRAFT.name(),
                currentUserId,
                "Draft bid details updated."
        );
        return toResponse(saved);
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
            throw new BusinessRuleException(
                    "Only a draft bid can be submitted."
            );
        }
        validatePackageOpenForBidding(bid.getWorkPackage());

        BidStatus oldStatus = bid.getStatus();
        bid.setStatus(BidStatus.SUBMITTED);
        bid.setSubmittedAt(LocalDateTime.now());
        Bid saved = bidRepository.save(bid);

        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Draft bid submitted."
        );
        notifyContractorAboutNewBid(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BidResponseDTO withdraw(
            Long bidId,
            Long currentCompanyId,
            Long currentUserId
    ) {
        Bid bid = getOwnedBid(bidId, currentCompanyId, true);

        if (bid.getStatus() != BidStatus.DRAFT
                && bid.getStatus() != BidStatus.SUBMITTED
                && bid.getStatus() != BidStatus.SHORTLISTED) {
            throw new BusinessRuleException(
                    "Only draft, submitted, or shortlisted bids can be withdrawn."
            );
        }
        if (bid.getWorkPackage().getStatus() == WorkPackageStatus.AWARDED) {
            throw new BusinessRuleException(
                    "A bid cannot be withdrawn after the work package is awarded."
            );
        }

        BidStatus oldStatus = bid.getStatus();
        bid.setStatus(BidStatus.WITHDRAWN);
        bid.setDecisionReason("Withdrawn by the bidder.");
        Bid saved = bidRepository.save(bid);

        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                "Bid withdrawn by the SME."
        );
        notifyContractorAboutWithdrawal(saved);
        return toResponse(saved);
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

        if (request.getDecision() != BidStatus.SHORTLISTED
                && request.getDecision() != BidStatus.REJECTED) {
            throw new BusinessRuleException(
                    "The supported decisions are SHORTLISTED and REJECTED."
            );
        }
        if (bid.getStatus() != BidStatus.SUBMITTED
                && bid.getStatus() != BidStatus.SHORTLISTED) {
            throw new BusinessRuleException(
                    "Only submitted or shortlisted bids can be evaluated."
            );
        }

        BidStatus oldStatus = bid.getStatus();
        bid.setStatus(request.getDecision());
        bid.setDecisionReason(trimToNull(request.getReason()));

        Bid saved = bidRepository.save(bid);
        recordHistory(
                saved.getId(),
                oldStatus.name(),
                saved.getStatus().name(),
                currentUserId,
                request.getReason() == null || request.getReason().isBlank()
                        ? "Bid evaluation decision recorded."
                        : request.getReason().trim()
        );

        notifyBidderAboutDecision(saved);
        return toResponse(saved);
    }

    @Override
    public BidResponseDTO getById(
            Long bidId,
            Long currentCompanyId
    ) {
        Bid bid = getBid(bidId);
        validateBidVisibility(bid, currentCompanyId);
        return toResponse(bid);
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
                .map(this::toResponse);
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

        AtomicLong rank = new AtomicLong(1L);

        return bidRepository
                .findByWorkPackageIdAndStatusInAndActiveTrue(
                        workPackageId,
                        COMPARISON_STATUSES
                )
                .stream()
                .sorted((first, second) -> {
                    int amountComparison =
                            first.getAmount().compareTo(second.getAmount());
                    if (amountComparison != 0) {
                        return amountComparison;
                    }
                    return first.getDeliveryDays()
                            .compareTo(second.getDeliveryDays());
                })
                .map(bid -> toComparison(bid, rank.getAndIncrement()))
                .toList();
    }


    @Override
    public BidDocumentDownloadDTO downloadDocument(
            Long documentId,
            Long currentCompanyId
    ) {
        BidDocument document =
                bidDocumentRepository
                        .findByIdAndActiveTrue(documentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Bid document not found with id: "
                                        + documentId
                        ));

        validateBidVisibility(
                document.getBid(),
                currentCompanyId
        );

        Path storedPath =
                uploadRootPath
                        .resolve(document.getFilePath())
                        .normalize();

        if (!storedPath.startsWith(
                uploadRootPath.normalize())) {
            throw new FileStorageException(
                    "Invalid bid document path."
            );
        }

        if (!Files.isRegularFile(storedPath)) {
            throw new ResourceNotFoundException(
                    "The stored bid document file is missing."
            );
        }

        return new BidDocumentDownloadDTO(
                storedPath,
                document.getFileName(),
                document.getContentType(),
                document.getFileSize()
        );
    }

    private Bid buildNewBid(
            BidRequestDTO request,
            Long currentCompanyId,
            BidStatus initialStatus
    ) {
        Company bidder = getCompany(currentCompanyId);
        validateBidderCompany(bidder);

        WorkPackage workPackage =
                getWorkPackage(request.getWorkPackageId());
        validatePackageOpenForBidding(workPackage);
        validateNotOwnWorkPackage(bidder, workPackage);
        validateEligibility(bidder, workPackage);
        ensureNoActiveDuplicateBid(bidder.getId(), workPackage.getId());

        Bid bid = new Bid();
        bid.setReferenceNumber(generateReference("BID"));
        bid.setBidderCompany(bidder);
        bid.setWorkPackage(workPackage);
        bid.setStatus(initialStatus);
        applyRequest(bid, request);

        return bid;
    }

    private void applyRequest(
            Bid bid,
            BidRequestDTO request
    ) {
        bid.setAmount(request.getAmount());
        bid.setDeliveryDays(request.getDeliveryDays());
        bid.setProposalText(request.getProposalText().trim());
        bid.setDecisionReason(null);
    }

    private void validateBidderCompany(Company bidder) {
        if (bidder.getVerificationStatus() == null
                || !"VERIFIED".equals(
                bidder.getVerificationStatus().name())) {
            throw new BusinessRuleException(
                    "Only a verified company can submit a bid."
            );
        }

        String companyType = bidder.getCompanyType() == null
                ? ""
                : bidder.getCompanyType().name();

        if (!"SUBCONTRACTOR".equals(companyType)
                && !"BOTH".equals(companyType)) {
            throw new BusinessRuleException(
                    "Only a subcontractor or dual-role company can submit a bid."
            );
        }
    }

    private void validatePackageOpenForBidding(
            WorkPackage workPackage
    ) {
        if (workPackage.getStatus() != WorkPackageStatus.OPEN) {
            throw new BusinessRuleException(
                    "Bids can only be submitted to an open work package."
            );
        }
        if (workPackage.getDeadline() == null
                || !workPackage.getDeadline().isAfter(
                LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "The work package bidding deadline has passed."
            );
        }
    }

    private void validateNotOwnWorkPackage(
            Company bidder,
            WorkPackage workPackage
    ) {
        Company contractor =
                workPackage.getProject().getContractorCompany();

        if (contractor != null
                && contractor.getId().equals(bidder.getId())) {
            throw new BusinessRuleException(
                    "A main contractor cannot bid on its own work package."
            );
        }
    }

    private void validateEligibility(
            Company bidder,
            WorkPackage workPackage
    ) {
        String eligibility =
                workPackage.getEligibilityType().name();

        if ("SME_ONLY".equals(eligibility)
                && !isSmeCompany(bidder)) {
            throw new BusinessRuleException(
                    "This work package is reserved for SMEs."
            );
        }

        if ("RIYADA_REQUIRED".equals(eligibility)
                && !hasVerifiedRiyadaRecord(bidder)) {
            throw new BusinessRuleException(
                    "This work package requires verified Riyada information."
            );
        }
    }

    private boolean isSmeCompany(Company company) {
        if (company.getCompanyType() == null) {
            return false;
        }
        String type = company.getCompanyType().name();
        return "SUBCONTRACTOR".equals(type) || "BOTH".equals(type);
    }

    private boolean hasVerifiedRiyadaRecord(Company company) {
        Long count = entityManager.createQuery(
                        "select count(document.id) from CompanyDocument document " +
                                "where document.company.id = :companyId " +
                                "and document.documentType = :documentType " +
                                "and document.verificationStatus = :verificationStatus " +
                                "and document.active = true",
                        Long.class
                )
                .setParameter("companyId", company.getId())
                .setParameter("documentType", DocumentType.RIYADA_CARD)
                .setParameter("verificationStatus", VerificationStatus.VERIFIED)
                .getSingleResult();

        return count != null && count > 0;
    }

    private void ensureNoActiveDuplicateBid(
            Long bidderCompanyId,
            Long workPackageId
    ) {
        if (bidRepository
                .existsByBidderCompanyIdAndWorkPackageIdAndStatusInAndActiveTrue(
                        bidderCompanyId,
                        workPackageId,
                        ACTIVE_DUPLICATE_STATUSES
                )) {
            throw new BusinessRuleException(
                    "Your company already has an active bid for this work package."
            );
        }
    }

    private void storeDocuments(
            Bid bid,
            BidRequestDTO request
    ) {
        List<MultipartFile> files =
                request.getDocuments() == null
                        ? List.of()
                        : request.getDocuments();

        if (files.isEmpty()) {
            return;
        }

        long existingCount =
                bidDocumentRepository.countByBidIdAndActiveTrue(
                        bid.getId()
                );

        if (existingCount + files.size()
                > MAX_DOCUMENTS_PER_BID) {
            throw new FileStorageException(
                    "A bid can contain a maximum of "
                            + MAX_DOCUMENTS_PER_BID
                            + " documents."
            );
        }

        if (request.getDocumentTypes() == null
                || request.getDocumentTypes().size()
                != files.size()) {
            throw new FileStorageException(
                    "Each uploaded document must have a document type."
            );
        }

        Path bidDirectory =
                uploadRootPath.resolve("bids")
                        .resolve(String.valueOf(bid.getId()))
                        .normalize();

        if (!bidDirectory.startsWith(
                uploadRootPath.normalize())) {
            throw new FileStorageException(
                    "Invalid bid document directory."
            );
        }

        List<Path> createdFiles = new ArrayList<>();

        try {
            Files.createDirectories(bidDirectory);

            for (int index = 0; index < files.size(); index++) {
                MultipartFile file = files.get(index);

                if (file == null || file.isEmpty()) {
                    throw new FileStorageException(
                            "An uploaded bid document is empty."
                    );
                }
                if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                    throw new FileStorageException(
                            "Each bid document must not exceed 5 MB."
                    );
                }

                String originalName = StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? "document"
                                : file.getOriginalFilename()
                );
                String storedName =
                        FileNameGenerator.generate(originalName);

                Path target =
                        FileNameGenerator.resolveSafePath(
                                bidDirectory,
                                storedName
                        );

                try (InputStream inputStream =
                             file.getInputStream()) {
                    Files.copy(
                            inputStream,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
                createdFiles.add(target);

                BidDocument document = new BidDocument();
                document.setFileName(originalName);
                document.setStoredFileName(storedName);
                document.setFilePath(
                        uploadRootPath.relativize(target)
                                .toString()
                                .replace("\\", "/")
                );
                document.setContentType(file.getContentType());
                document.setFileSize(file.getSize());
                document.setDocumentType(
                        request.getDocumentTypes().get(index)
                );
                document.setBid(bid);

                bidDocumentRepository.save(document);
                bid.addDocument(document);
            }
        } catch (IOException exception) {
            createdFiles.forEach(this::deleteQuietly);
            throw new FileStorageException(
                    "Failed to store one or more bid documents.",
                    exception
            );
        } catch (RuntimeException exception) {
            createdFiles.forEach(this::deleteQuietly);
            throw exception;
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The original storage failure is more important.
        }
    }

    private void validateBidVisibility(
            Bid bid,
            Long currentCompanyId
    ) {
        Long bidderCompanyId =
                bid.getBidderCompany().getId();

        Long contractorCompanyId =
                bid.getWorkPackage()
                        .getProject()
                        .getContractorCompany()
                        .getId();

        if (!currentCompanyId.equals(bidderCompanyId)
                && !currentCompanyId.equals(contractorCompanyId)) {
            throw new UnauthorizedOperationException(
                    "You cannot view this bid."
            );
        }
    }

    private Bid getOwnedBid(
            Long id,
            Long currentCompanyId,
            boolean lock
    ) {
        Bid bid =
                (lock
                        ? bidRepository.findByIdForUpdate(id)
                        : bidRepository.findByIdAndActiveTrue(id))
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Bid not found with id: " + id
                        ));

        ownershipValidator.validateBidOwnership(
                currentCompanyId,
                bid
        );
        return bid;
    }

    private Bid getBid(Long id) {
        return bidRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bid not found with id: " + id
                ));
    }

    private WorkPackage getWorkPackage(Long id) {
        return workPackageRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work package not found with id: " + id
                ));
    }

    private Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + id
                ));
    }

    private void notifyContractorAboutNewBid(Bid bid) {
        Company contractor =
                bid.getWorkPackage()
                        .getProject()
                        .getContractorCompany();

        if (contractor != null
                && contractor.getUser() != null) {
            notificationService.create(
                    contractor.getUser().getId(),
                    "New bid received",
                    "A new bid was submitted for "
                            + bid.getWorkPackage().getTitle()
                            + ".",
                    NotificationType.NEW_BID
            );
        }
    }

    private void notifyContractorAboutWithdrawal(
            Bid bid
    ) {
        Company contractor =
                bid.getWorkPackage()
                        .getProject()
                        .getContractorCompany();

        if (contractor != null
                && contractor.getUser() != null) {
            notificationService.create(
                    contractor.getUser().getId(),
                    "Bid withdrawn",
                    bid.getBidderCompany().getLegalName()
                            + " withdrew its bid for "
                            + bid.getWorkPackage().getTitle()
                            + ".",
                    NotificationType.BID_STATUS
            );
        }
    }

    private void notifyBidderAboutDecision(Bid bid) {
        Company bidder = bid.getBidderCompany();

        if (bidder.getUser() != null) {
            notificationService.create(
                    bidder.getUser().getId(),
                    "Bid status updated",
                    "Your bid "
                            + bid.getReferenceNumber()
                            + " is now "
                            + bid.getStatus().name()
                            + ".",
                    NotificationType.BID_STATUS
            );
        }
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
        history.setEntityType(HistoryEntityType.BID);
        history.setEntityId(entityId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(user);
        history.setNote(note);

        statusHistoryRepository.save(history);
    }

    private BidResponseDTO toResponse(Bid bid) {
        List<BidResponseDTO.DocumentItem> documents =
                bidDocumentRepository
                        .findByBidIdAndActiveTrueOrderByCreatedAtAsc(
                                bid.getId()
                        )
                        .stream()
                        .map(document ->
                                BidResponseDTO.DocumentItem.builder()
                                        .id(document.getId())
                                        .fileName(document.getFileName())
                                        .contentType(
                                                document.getContentType()
                                        )
                                        .fileSize(document.getFileSize())
                                        .documentType(
                                                document.getDocumentType()
                                        )
                                        .uploadedAt(
                                                document.getCreatedAt()
                                        )
                                        .build()
                        )
                        .toList();

        Company bidder = bid.getBidderCompany();
        WorkPackage workPackage = bid.getWorkPackage();

        return BidResponseDTO.builder()
                .id(bid.getId())
                .referenceNumber(bid.getReferenceNumber())
                .amount(bid.getAmount())
                .deliveryDays(bid.getDeliveryDays())
                .proposalText(bid.getProposalText())
                .submittedAt(bid.getSubmittedAt())
                .status(bid.getStatus())
                .decisionReason(bid.getDecisionReason())
                .bidderCompanyId(bidder.getId())
                .bidderCompanyName(companyName(bidder))
                .bidderVerified(
                        bidder.getVerificationStatus() != null
                                && "VERIFIED".equals(
                                bidder.getVerificationStatus().name()
                        )
                )
                .workPackageId(workPackage.getId())
                .workPackageReferenceNumber(
                        workPackage.getReferenceNumber()
                )
                .workPackageTitle(workPackage.getTitle())
                .documents(documents)
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }

    private BidComparisonResponseDTO toComparison(
            Bid bid,
            long rank
    ) {
        Company bidder = bid.getBidderCompany();

        return BidComparisonResponseDTO.builder()
                .rank(rank)
                .bidId(bid.getId())
                .referenceNumber(bid.getReferenceNumber())
                .bidderCompanyId(bidder.getId())
                .bidderCompanyName(companyName(bidder))
                .bidderVerified(
                        bidder.getVerificationStatus() != null
                                && "VERIFIED".equals(
                                bidder.getVerificationStatus().name()
                        )
                )
                .amount(bid.getAmount())
                .deliveryDays(bid.getDeliveryDays())
                .proposalText(bid.getProposalText())
                .status(bid.getStatus())
                .submittedAt(bid.getSubmittedAt())
                .documentCount(
                        bidDocumentRepository
                                .countByBidIdAndActiveTrue(
                                        bid.getId()
                                )
                )
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
