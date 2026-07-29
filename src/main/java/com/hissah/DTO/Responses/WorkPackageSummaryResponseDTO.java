package com.hissah.DTO.Responses;

import com.hissah.Enums.EligibilityType;
import com.hissah.Enums.WorkPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkPackageSummaryResponseDTO {
    private Long id;
    private String referenceNumber;
    private String title;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDateTime deadline;
    private String location;
    private WorkPackageStatus status;
    private EligibilityType eligibilityType;
    private Long projectId;
    private String projectTitle;
    private Long categoryId;
    private String categoryName;
    private Long contractorCompanyId;
    private String contractorCompanyName;
    private Long bidCount;
    private LocalDateTime publishedAt;
}
