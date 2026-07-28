package com.hissah.Services;

import com.hissah.DTOs.Requests.MilestoneProgressRequestDTO;
import com.hissah.DTOs.Requests.MilestoneRequestDTO;
import com.hissah.DTOs.Responses.MilestoneResponseDTO;

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
