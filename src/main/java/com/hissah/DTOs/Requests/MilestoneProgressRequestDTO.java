package com.hissah.DTOs.Requests;

import com.hissah.Enums.MilestoneStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneProgressRequestDTO {

    @NotNull(message = "Completion percentage is required.")
    @Min(value = 0, message = "Completion percentage cannot be less than 0.")
    @Max(value = 100, message = "Completion percentage cannot exceed 100.")
    private Integer completionPercent;

    @NotNull(message = "Milestone status is required.")
    private MilestoneStatus status;

    @Size(max = 10000, message = "Evidence note is too long.")
    private String evidenceNote;
}
