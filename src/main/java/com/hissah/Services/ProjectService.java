package com.hissah.Services;

import com.hissah.Enums.VerificationStatus;
import com.hissah.Entities.Company;
import com.hissah.Entities.Project;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyService companyService;


    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }


    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }


    @Transactional
    public Project createProject(Project project, Long contractorCompanyId) {
        Company contractor = companyService.getCompanyById(contractorCompanyId);

        if (contractor.getVerificationStatus() == null || contractor.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Project creation restricted: Contractor company must be a verified main contractor.");
        }

        project.setContractorCompanyId(contractorCompanyId);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long id, Project projectDetails, Long contractorCompanyId) {
        Project project = getProjectById(id);

        Company contractor = companyService.getCompanyById(contractorCompanyId);
        if (contractor.getVerificationStatus() == null || contractor.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Project update restricted: Contractor company must be a verified main contractor.");
        }

        project.setTitle(projectDetails.getTitle());
        project.setReferenceNumber(projectDetails.getReferenceNumber());
        project.setSector(projectDetails.getSector());
        project.setLocation(projectDetails.getLocation());
        project.setDescription(projectDetails.getDescription());
        project.setStartDate(projectDetails.getStartDate());
        project.setEndDate(projectDetails.getEndDate());
        project.setContractorCompanyId(contractorCompanyId);

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
    }
}
