package com.hissah.Services;

import com.hissah.DTO.Requests.MilestoneProgressRequestDTO;
import com.hissah.DTO.Requests.MilestoneRequestDTO;
import com.hissah.DTO.Responses.MilestoneResponseDTO;

import java.util.List;

public interface MilestoneService {
    MilestoneResponseDTO create(
            MilestoneRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId);

    MilestoneResponseDTO update(
            Long milestoneId,
            MilestoneRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId);

    MilestoneResponseDTO updateProgress(
            Long milestoneId,
            MilestoneProgressRequestDTO request,
            Long subcontractorCompanyId,
            Long currentUserId);

    MilestoneResponseDTO submit(
            Long milestoneId,
            Long subcontractorCompanyId,
            Long currentUserId);

    MilestoneResponseDTO approve(
            Long milestoneId,
            Long contractorCompanyId,
            Long currentUserId);

    List<MilestoneResponseDTO> getByAward(
            Long awardId, Long currentCompanyId);
}
