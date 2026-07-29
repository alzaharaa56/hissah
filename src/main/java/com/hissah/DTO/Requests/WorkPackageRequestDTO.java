package com.hissah.DTO.Requests;

import com.hissah.Enums.EligibilityType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class WorkPackageRequestDTO {

    @NotNull(message = "Project ID is required.")
    private Long projectId;

    @NotNull(message = "Category ID is required.")
    private Long categoryId;

    @NotBlank(message = "Title is required.")
    @Size(max = 180, message = "Title must not exceed 180 characters.")
    private String title;

    @NotBlank(message = "Scope is required.")
    @Size(max = 10000, message = "Scope is too long.")
    private String scope;

    @Size(max = 10000, message = "Requirements are too long.")
    private String requirements;

    @NotNull(message = "Minimum budget is required.")
    @DecimalMin(value = "0.000", message = "Minimum budget cannot be negative.")
    private BigDecimal budgetMin;

    @NotNull(message = "Maximum budget is required.")
    @DecimalMin(value = "0.000", message = "Maximum budget cannot be negative.")
    private BigDecimal budgetMax;

    @NotNull(message = "Deadline is required.")
    @Future(message = "Deadline must be in the future.")
    private LocalDateTime deadline;

    @NotBlank(message = "Location is required.")
    @Size(max = 120, message = "Location must not exceed 120 characters.")
    private String location;

    @NotNull(message = "Eligibility type is required.")
    private EligibilityType eligibilityType;

    @AssertTrue(message = "Maximum budget must be greater than or equal to minimum budget.")
    public boolean isBudgetRangeValid() {
        return budgetMin == null
                || budgetMax == null
                || budgetMax.compareTo(budgetMin) >= 0;
    }
}
