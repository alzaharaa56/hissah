package com.hissah.DTO.Response;

import com.hissah.Enums.ProjectStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectSummaryResponseDTO {

    private Long id;
    private String title;
    private String referenceNumber;
    private String sector;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private Integer workPackageCount;
    private LocalDateTime createdAt;
}
