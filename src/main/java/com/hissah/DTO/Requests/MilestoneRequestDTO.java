package com.hissah.DTO.Requests;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneRequestDTO {

    @NotNull(message = "Award ID is required.")
    private Long awardId;

    @NotBlank(message = "Milestone title is required.")
    @Size(max = 180, message = "Milestone title must not exceed 180 characters.")
    private String title;

    @Size(max = 10000, message = "Milestone description is too long.")
    private String description;

    @NotNull(message = "Milestone due date is required.")
    @FutureOrPresent(message = "Milestone due date cannot be in the past.")
    private LocalDate dueDate;
}
