package com.hissah.Services;

import com.hissah.DTO.Response.AiSubcontractorMatchResponseDTO;

import java.util.List;

public interface AiSubcontractorMatchingService {

    List<AiSubcontractorMatchResponseDTO> findMatches(
            Long workPackageId,
            Long contractorCompanyId,
            int limit
    );
}
