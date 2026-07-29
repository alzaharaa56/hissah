package com.hissah.DTO.Responses;

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
public class AwardResponseDTO {
    private Long id;
    private String referenceNumber;
    private LocalDateTime awardedAt;
    private BigDecimal agreedAmount;
    private Integer agreedDeliveryDays;
    private String notes;
    private AwardStatus status;
    private Long bidId;
    private String bidReferenceNumber;
    private Long workPackageId;
    private String workPackageReferenceNumber;
    private String workPackageTitle;
    private Long subcontractorCompanyId;
    private String subcontractorCompanyName;
    private Long contractorCompanyId;
    private String contractorCompanyName;

    @Builder.Default
    private List<MilestoneResponseDTO> milestones = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
