package com.hissah.Services;

import com.hissah.DTO.Requests.AwardRequestDTO;
import com.hissah.DTO.Responses.AwardResponseDTO;

import java.util.List;

public interface AwardService {
    AwardResponseDTO awardBid(
            AwardRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId);

    AwardResponseDTO getById(Long awardId, Long currentCompanyId);

    AwardResponseDTO getByWorkPackage(
            Long workPackageId, Long currentCompanyId);

    List<AwardResponseDTO> getMyAwards(Long currentCompanyId);
}
