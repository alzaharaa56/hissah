package com.hissah.Services.Implementations;

import com.hissah.DTO.Response.BidComparisonResponseDTO;
import com.hissah.DTO.Response.DashboardResponseDTO;
import com.hissah.DTO.Response.WorkPackageSummaryResponseDTO;
import com.hissah.Entities.Award;
import com.hissah.Entities.Bid;
import com.hissah.Entities.Company;
import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.AwardStatus;
import com.hissah.Enums.BidStatus;
import com.hissah.Enums.MilestoneStatus;
import com.hissah.Enums.Role;
import com.hissah.Enums.WorkPackageStatus;
import com.hissah.Repositories.AwardRepository;
import com.hissah.Repositories.BidDocumentRepository;
import com.hissah.Repositories.BidRepository;
import com.hissah.Repositories.CompanyRepository;
import com.hissah.Repositories.MilestoneRepository;
import com.hissah.Repositories.ProjectRepository;
import com.hissah.Repositories.WorkPackageRepository;
import com.hissah.Services.Implementations.Support.CompanyReferenceSupport;
import com.hissah.Services.NotificationService;
import com.hissah.Services.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingServiceImpl implements ReportingService {

    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final BidRepository bidRepository;
    private final BidDocumentRepository bidDocumentRepository;
    private final AwardRepository awardRepository;
    private final MilestoneRepository milestoneRepository;
    private final NotificationService notificationService;
    private final CompanyReferenceSupport companyReferenceSupport;

    @Override
    public DashboardResponseDTO getDashboard(
            Long currentUserId,
            Long currentCompanyId,
            Role role
    ) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "A dashboard role is required."
            );
        }

        String roleName = role.name();

        if ("ADMIN".equals(roleName)) {
            return buildAdminDashboard(
                    currentUserId,
                    roleName
            );
        }
        if ("MAIN_CONTRACTOR".equals(roleName)) {
            requireCompanyId(currentCompanyId);
            return buildContractorDashboard(
                    currentUserId,
                    currentCompanyId,
                    roleName
            );
        }
        if ("SUBCONTRACTOR".equals(roleName)) {
            requireCompanyId(currentCompanyId);
            return buildSubcontractorDashboard(
                    currentUserId,
                    currentCompanyId,
                    roleName
            );
        }

        throw new IllegalArgumentException(
                "Unsupported dashboard role: " + roleName
        );
    }

    private DashboardResponseDTO buildAdminDashboard(
            Long userId,
            String roleName
    ) {
        List<WorkPackage> recentPackages =
                workPackageRepository
                        .findTop5ByActiveTrueOrderByCreatedAtDesc();

        List<Bid> recentBids =
                bidRepository
                        .findTop5ByActiveTrueOrderByCreatedAtDesc();

        List<Award> recentAwards =
                awardRepository
                        .findTop5ByActiveTrueOrderByAwardedAtDesc();

        return DashboardResponseDTO.builder()
                .role(roleName)
                .totalCompanies(companyRepository.count())
                .totalProjects(projectRepository.count())
                .totalWorkPackages(
                        workPackageRepository.count()
                )
                .openWorkPackages(
                        workPackageRepository
                                .countByStatusAndActiveTrue(
                                        WorkPackageStatus.OPEN
                                )
                )
                .totalBids(bidRepository.count())
                .submittedBids(
                        bidRepository.countByStatusAndActiveTrue(
                                BidStatus.SUBMITTED
                        )
                                + bidRepository
                                .countByStatusAndActiveTrue(
                                        BidStatus.SHORTLISTED
                                )
                )
                .activeAwards(
                        awardRepository
                                .countByStatusAndActiveTrue(
                                        AwardStatus.ACTIVE
                                )
                )
                .pendingMilestones(
                        countAdminPendingMilestones()
                )
                .unreadNotifications(
                        notificationService.countUnread(userId)
                )
                .recentWorkPackages(
                        recentPackages.stream()
                                .map(this::toWorkPackageSummary)
                                .toList()
                )
                .recentBids(toBidSummaries(recentBids))
                .recentAwards(
                        recentAwards.stream()
                                .map(this::toAwardSummary)
                                .toList()
                )
                .recentNotifications(
                        notificationService.getRecent(userId)
                )
                .build();
    }

    private DashboardResponseDTO buildContractorDashboard(
            Long userId,
            Long companyId,
            String roleName
    ) {
        List<WorkPackage> recentPackages =
                workPackageRepository
                        .findTop5ByProjectContractorCompanyIdAndActiveTrueOrderByCreatedAtDesc(
                                companyId
                        );

        List<Bid> recentBids =
                bidRepository
                        .findTop5ByWorkPackageProjectContractorCompanyIdAndActiveTrueOrderByCreatedAtDesc(
                                companyId
                        );

        List<Award> recentAwards =
                awardRepository
                        .findTop5ByWorkPackageProjectContractorCompanyIdAndActiveTrueOrderByAwardedAtDesc(
                                companyId
                        );

        return DashboardResponseDTO.builder()
                .role(roleName)
                .totalCompanies(null)
                .totalProjects(null)
                .totalWorkPackages(
                        workPackageRepository
                                .countByProjectContractorCompanyIdAndActiveTrue(
                                        companyId
                                )
                )
                .openWorkPackages(
                        workPackageRepository
                                .countByProjectContractorCompanyIdAndStatusAndActiveTrue(
                                        companyId,
                                        WorkPackageStatus.OPEN
                                )
                )
                .totalBids(
                        bidRepository
                                .countByWorkPackageProjectContractorCompanyIdAndActiveTrue(
                                        companyId
                                )
                )
                .submittedBids(
                        countContractorBids(
                                companyId,
                                BidStatus.SUBMITTED
                        )
                                + countContractorBids(
                                companyId,
                                BidStatus.SHORTLISTED
                        )
                )
                .activeAwards(
                        awardRepository
                                .countByWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
                                        companyId,
                                        AwardStatus.ACTIVE
                                )
                )
                .pendingMilestones(
                        countContractorPendingMilestones(
                                companyId
                        )
                )
                .unreadNotifications(
                        notificationService.countUnread(userId)
                )
                .recentWorkPackages(
                        recentPackages.stream()
                                .map(this::toWorkPackageSummary)
                                .toList()
                )
                .recentBids(toBidSummaries(recentBids))
                .recentAwards(
                        recentAwards.stream()
                                .map(this::toAwardSummary)
                                .toList()
                )
                .recentNotifications(
                        notificationService.getRecent(userId)
                )
                .build();
    }

    private DashboardResponseDTO buildSubcontractorDashboard(
            Long userId,
            Long companyId,
            String roleName
    ) {
        List<Bid> recentBids =
                bidRepository
                        .findTop5ByBidderCompanyIdAndActiveTrueOrderByCreatedAtDesc(
                                companyId
                        );

        List<Award> recentAwards =
                awardRepository
                        .findTop5ByBidBidderCompanyIdAndActiveTrueOrderByAwardedAtDesc(
                                companyId
                        );

        return DashboardResponseDTO.builder()
                .role(roleName)
                .totalCompanies(null)
                .totalProjects(null)
                .totalWorkPackages(null)
                .openWorkPackages(
                        workPackageRepository
                                .countByStatusAndActiveTrue(
                                        WorkPackageStatus.OPEN
                                )
                )
                .totalBids(
                        bidRepository
                                .countByBidderCompanyIdAndActiveTrue(
                                        companyId
                                )
                )
                .submittedBids(
                        bidRepository
                                .countByBidderCompanyIdAndStatusAndActiveTrue(
                                        companyId,
                                        BidStatus.SUBMITTED
                                )
                                + bidRepository
                                .countByBidderCompanyIdAndStatusAndActiveTrue(
                                        companyId,
                                        BidStatus.SHORTLISTED
                                )
                )
                .activeAwards(
                        awardRepository
                                .countByBidBidderCompanyIdAndStatusAndActiveTrue(
                                        companyId,
                                        AwardStatus.ACTIVE
                                )
                )
                .pendingMilestones(
                        countSubcontractorPendingMilestones(
                                companyId
                        )
                )
                .unreadNotifications(
                        notificationService.countUnread(userId)
                )
                .recentWorkPackages(new ArrayList<>())
                .recentBids(toBidSummaries(recentBids))
                .recentAwards(
                        recentAwards.stream()
                                .map(this::toAwardSummary)
                                .toList()
                )
                .recentNotifications(
                        notificationService.getRecent(userId)
                )
                .build();
    }

    private long countContractorBids(
            Long companyId,
            BidStatus status
    ) {
        return bidRepository
                .countByWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        status
                );
    }

    private long countAdminPendingMilestones() {
        return milestoneRepository
                .countByStatusAndActiveTrue(
                        MilestoneStatus.NOT_STARTED
                )
                + milestoneRepository
                .countByStatusAndActiveTrue(
                        MilestoneStatus.IN_PROGRESS
                )
                + milestoneRepository
                .countByStatusAndActiveTrue(
                        MilestoneStatus.SUBMITTED
                )
                + milestoneRepository
                .countByStatusAndActiveTrue(
                        MilestoneStatus.OVERDUE
                );
    }

    private long countContractorPendingMilestones(
            Long companyId
    ) {
        return milestoneRepository
                .countByAwardWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.NOT_STARTED
                )
                + milestoneRepository
                .countByAwardWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.IN_PROGRESS
                )
                + milestoneRepository
                .countByAwardWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.SUBMITTED
                )
                + milestoneRepository
                .countByAwardWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.OVERDUE
                );
    }

    private long countSubcontractorPendingMilestones(
            Long companyId
    ) {
        return milestoneRepository
                .countByAwardBidBidderCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.NOT_STARTED
                )
                + milestoneRepository
                .countByAwardBidBidderCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.IN_PROGRESS
                )
                + milestoneRepository
                .countByAwardBidBidderCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.SUBMITTED
                )
                + milestoneRepository
                .countByAwardBidBidderCompanyIdAndStatusAndActiveTrue(
                        companyId,
                        MilestoneStatus.OVERDUE
                );
    }

    private WorkPackageSummaryResponseDTO toWorkPackageSummary(
            WorkPackage workPackage
    ) {
        Company contractor =
                companyReferenceSupport.requireProjectContractor(workPackage.getProject());

        return WorkPackageSummaryResponseDTO.builder()
                .id(workPackage.getId())
                .referenceNumber(workPackage.getReferenceNumber())
                .title(workPackage.getTitle())
                .budgetMin(workPackage.getBudgetMin())
                .budgetMax(workPackage.getBudgetMax())
                .deadline(workPackage.getDeadline())
                .location(workPackage.getLocation())
                .status(workPackage.getStatus())
                .eligibilityType(
                        workPackage.getEligibilityType()
                )
                .projectId(workPackage.getProject().getId())
                .projectTitle(
                        workPackage.getProject().getTitle()
                )
                .categoryId(workPackage.getCategory().getId())
                .categoryName(
                        workPackage.getCategory().getName()
                )
                .contractorCompanyId(contractor.getId())
                .contractorCompanyName(
                        companyName(contractor)
                )
                .bidCount(
                        bidRepository
                                .countByWorkPackageIdAndActiveTrue(
                                        workPackage.getId()
                                )
                )
                .publishedAt(workPackage.getPublishedAt())
                .build();
    }

    private List<BidComparisonResponseDTO> toBidSummaries(
            List<Bid> bids
    ) {
        AtomicLong rank = new AtomicLong(1L);

        return bids.stream()
                .map(bid ->
                        BidComparisonResponseDTO.builder()
                                .rank(rank.getAndIncrement())
                                .bidId(bid.getId())
                                .referenceNumber(
                                        bid.getReferenceNumber()
                                )
                                .bidderCompanyId(
                                        bid.getBidderCompany().getId()
                                )
                                .bidderCompanyName(
                                        companyName(
                                                bid.getBidderCompany()
                                        )
                                )
                                .bidderVerified(
                                        bid.getBidderCompany()
                                                .getVerificationStatus()
                                                != null
                                                && "VERIFIED".equals(
                                                bid.getBidderCompany()
                                                        .getVerificationStatus()
                                                        .name()
                                        )
                                )
                                .amount(bid.getAmount())
                                .deliveryDays(
                                        bid.getDeliveryDays()
                                )
                                .proposalText(
                                        bid.getProposalText()
                                )
                                .status(bid.getStatus())
                                .submittedAt(
                                        bid.getSubmittedAt()
                                )
                                .documentCount(
                                        bidDocumentRepository
                                                .countByBidIdAndActiveTrue(
                                                        bid.getId()
                                                )
                                )
                                .build()
                )
                .toList();
    }

    private DashboardResponseDTO.AwardSummary toAwardSummary(
            Award award
    ) {
        return DashboardResponseDTO.AwardSummary.builder()
                .awardId(award.getId())
                .referenceNumber(award.getReferenceNumber())
                .workPackageTitle(
                        award.getWorkPackage().getTitle()
                )
                .subcontractorCompanyName(
                        companyName(
                                award.getBid().getBidderCompany()
                        )
                )
                .agreedAmount(award.getAgreedAmount())
                .status(award.getStatus())
                .awardedAt(award.getAwardedAt())
                .build();
    }

    private String companyName(Company company) {
        if (company.getTradingName() != null
                && !company.getTradingName().isBlank()) {
            return company.getTradingName();
        }
        return company.getLegalName();
    }

    private void requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException(
                    "The authenticated user is not linked to a company."
            );
        }
    }
}
