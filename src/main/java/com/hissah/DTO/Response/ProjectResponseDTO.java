package com.hissah.DTO.Response;

import com.hissah.Entities.Project;
import lombok.Data;

import java.time.LocalDate;

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
    private CompanySummaryResponseDTO contractorCompany;
    private Integer packageCount;

    public static ProjectResponseDTO fromEntity(Project project, CompanySummaryResponseDTO contractorSummary, Integer packageCount) {
        if (project == null) return null;
        ProjectResponseDTO response = new ProjectResponseDTO();
        response.setId(project.getId());
        response.setTitle(project.getTitle());
        response.setReferenceNumber(project.getReferenceNumber());
        response.setSector(project.getSector());
        response.setLocation(project.getLocation());
        response.setDescription(project.getDescription());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setContractorCompany(contractorSummary);
        response.setPackageCount(packageCount != null ? packageCount : 0);
        return response;
    }
}