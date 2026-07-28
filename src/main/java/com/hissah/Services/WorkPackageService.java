package com.hissah.Services;

import com.hissah.DTO.Requests.WorkPackageRequestDTO;
import com.hissah.DTO.Requests.WorkPackageSearchRequestDTO;
import com.hissah.DTO.Responses.WorkPackageResponseDTO;
import com.hissah.DTO.Responses.WorkPackageSummaryResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WorkPackageService {
    WorkPackageResponseDTO create(
            WorkPackageRequestDTO request, Long currentCompanyId, Long currentUserId);

    WorkPackageResponseDTO update(
            Long workPackageId,
            WorkPackageRequestDTO request,
            Long currentCompanyId,
            Long currentUserId);

    WorkPackageResponseDTO publish(
            Long workPackageId, Long currentCompanyId, Long currentUserId);

    WorkPackageResponseDTO close(
            Long workPackageId, Long currentCompanyId, Long currentUserId);

    WorkPackageResponseDTO cancel(
            Long workPackageId,
            String reason,
            Long currentCompanyId,
            Long currentUserId);

    WorkPackageResponseDTO getById(Long workPackageId, Long viewerCompanyId);

    List<WorkPackageSummaryResponseDTO> getByProject(
            Long projectId, Long currentCompanyId);

    Page<WorkPackageSummaryResponseDTO> search(
            WorkPackageSearchRequestDTO request);
}
