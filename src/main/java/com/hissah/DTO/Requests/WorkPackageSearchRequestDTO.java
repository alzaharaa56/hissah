package com.hissah.DTOs.Requests;

import com.hissah.Enums.EligibilityType;
import com.hissah.Enums.WorkPackageStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
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
public class WorkPackageSearchRequestDTO {

    private String keyword;
    private Long projectId;
    private Long categoryId;
    private String location;

    @PositiveOrZero(message = "Minimum budget cannot be negative.")
    private BigDecimal budgetMin;

    @PositiveOrZero(message = "Maximum budget cannot be negative.")
    private BigDecimal budgetMax;

    private LocalDateTime deadlineFrom;
    private LocalDateTime deadlineTo;
    private EligibilityType eligibilityType;
    private WorkPackageStatus status;

    @Builder.Default
    @Min(value = 0, message = "Page number cannot be negative.")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1.")
    @Max(value = 100, message = "Page size cannot exceed 100.")
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "publishedAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
