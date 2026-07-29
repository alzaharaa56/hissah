package com.hissah.DTO.Response;

import com.hissah.Enums.AwardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private String role;
    private Long totalCompanies;
    private Long totalProjects;
    private Long totalWorkPackages;
    private Long openWorkPackages;
    private Long totalBids;
    private Long submittedBids;
    private Long activeAwards;
    private Long pendingMilestones;
    private Long unreadNotifications;

    @Builder.Default
    private List<WorkPackageSummaryResponseDTO> recentWorkPackages = new ArrayList<>();

    @Builder.Default
    private List<BidComparisonResponseDTO> recentBids = new ArrayList<>();

    @Builder.Default
    private List<AwardSummary> recentAwards = new ArrayList<>();

    @Builder.Default
    private List<NotificationResponseDTO> recentNotifications = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AwardSummary {
        private Long awardId;
        private String referenceNumber;
        private String workPackageTitle;
        private String subcontractorCompanyName;
        private BigDecimal agreedAmount;
        private AwardStatus status;
        private LocalDateTime awardedAt;
    }
}
