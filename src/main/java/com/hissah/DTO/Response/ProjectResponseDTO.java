package com.hissah.DTO.Response;

import com.hissah.Enums.ProjectStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectResponseDTO {

    private Long id;
    private String title;
    private String referenceNumber;
    private String sector;
    private String location;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private Long contractorCompanyId;
    private CompanySummaryResponseDTO contractor;
    private Integer workPackageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
