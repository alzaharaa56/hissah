package com.hissah.Services;

import com.hissah.DTO.Request.ProjectRequestDTO;
import com.hissah.DTO.Response.ProjectResponseDTO;
import com.hissah.DTO.Response.ProjectSummaryResponseDTO;
import com.hissah.Entities.Project;
import com.hissah.Enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponseDTO create(
            ProjectRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    );

    ProjectResponseDTO update(
            Long projectId,
            ProjectRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId
    );

    ProjectResponseDTO activate(
            Long projectId,
            Long contractorCompanyId,
            Long currentUserId
    );

    ProjectResponseDTO complete(
            Long projectId,
            Long contractorCompanyId,
            Long currentUserId
    );

    ProjectResponseDTO cancel(
            Long projectId,
            String reason,
            Long contractorCompanyId,
            Long currentUserId
    );

    ProjectResponseDTO getById(
            Long projectId,
            Long currentCompanyId,
            Role currentRole
    );

    Page<ProjectSummaryResponseDTO> getMyProjects(
            Long contractorCompanyId,
            Pageable pageable
    );

    Project getOwnedProjectEntity(
            Long projectId,
            Long contractorCompanyId
    );
}

