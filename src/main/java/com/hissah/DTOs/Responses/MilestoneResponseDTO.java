package com.hissah.DTOs.Responses;

import com.hissah.Enums.MilestoneStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponseDTO {
    private Long id;
    private Long awardId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer completionPercent;
    private MilestoneStatus status;
    private String evidenceNote;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private Boolean overdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
