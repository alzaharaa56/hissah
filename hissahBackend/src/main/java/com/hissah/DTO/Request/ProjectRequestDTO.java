package com.hissah.DTO.Request;

import com.hissah.Entities.Project;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequestDTO {
    private String title;
    private String referenceNumber;
    private String sector;
    private String location;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long contractorCompanyId;

    public Project toEntity() {
        Project project = new Project();
        project.setTitle(this.title);
        project.setReferenceNumber(this.referenceNumber);
        project.setSector(this.sector);
        project.setLocation(this.location);
        project.setDescription(this.description);
        project.setStartDate(this.startDate);
        project.setEndDate(this.endDate);
        project.setContractorCompanyId(this.contractorCompanyId);
        return project;
    }

    public void updateEntity(Project project) {
        if (project == null) return;
        if (this.title != null) project.setTitle(this.title);
        if (this.referenceNumber != null) project.setReferenceNumber(this.referenceNumber);
        if (this.sector != null) project.setSector(this.sector);
        if (this.location != null) project.setLocation(this.location);
        if (this.description != null) project.setDescription(this.description);
        if (this.startDate != null) project.setStartDate(this.startDate);
        if (this.endDate != null) project.setEndDate(this.endDate);
        if (this.contractorCompanyId != null) project.setContractorCompanyId(this.contractorCompanyId);
    }
}
