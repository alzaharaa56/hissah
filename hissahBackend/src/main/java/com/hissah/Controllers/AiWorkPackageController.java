package com.hissah.Controllers;

import com.hissah.DTO.Request.AiWorkPackageRequestDTO;
import com.hissah.DTO.Response.AiWorkPackageResponseDTO;
import com.hissah.Services.AiWorkPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/work-packages")
@RequiredArgsConstructor
public class AiWorkPackageController {

    private final AiWorkPackageService aiWorkPackageService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<AiWorkPackageResponseDTO> generate(
            @Valid @RequestBody AiWorkPackageRequestDTO request
    ) {
        return ResponseEntity.ok(
                aiWorkPackageService.generateDraft(request)
        );
    }
}
