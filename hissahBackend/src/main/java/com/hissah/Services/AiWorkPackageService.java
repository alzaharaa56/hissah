package com.hissah.Services;

import com.hissah.DTO.Request.AiWorkPackageRequestDTO;
import com.hissah.DTO.Response.AiWorkPackageResponseDTO;

public interface AiWorkPackageService {

    AiWorkPackageResponseDTO generateDraft(
            AiWorkPackageRequestDTO request
    );
}
