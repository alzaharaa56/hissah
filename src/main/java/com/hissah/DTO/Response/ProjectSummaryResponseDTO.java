package com.hissah.DTO.Response;

import com.hissah.Entities.Project;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectSummaryResponseDTO {
    private Long id;
    private String title;
    private String referenceNumber;
    private String sector;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractorName;

    public static ProjectSummaryResponseDTO fromEntity(Project project, String contractorName) {
        if (project == null) return null;
        ProjectSummaryResponseDTO summary = new ProjectSummaryResponseDTO();
        summary.setId(project.getId());
        summary.setTitle(project.getTitle());
        summary.setReferenceNumber(project.getReferenceNumber());
        summary.setSector(project.getSector());
        summary.setLocation(project.getLocation());
        summary.setStartDate(project.getStartDate());
        summary.setEndDate(project.getEndDate());
        summary.setContractorName(contractorName);
        return summary;
    }
}
