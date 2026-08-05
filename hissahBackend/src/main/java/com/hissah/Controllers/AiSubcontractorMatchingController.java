package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.DTO.Response.AiSubcontractorMatchResponseDTO;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.AiSubcontractorMatchingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/work-packages")
@RequiredArgsConstructor
@Validated
public class AiSubcontractorMatchingController {

    private final AiSubcontractorMatchingService matchingService;

    @GetMapping("/{workPackageId}/matches")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<List<AiSubcontractorMatchResponseDTO>> findMatches(
            @PathVariable @Positive Long workPackageId,
            @RequestParam(defaultValue = "5")
            @Min(1) @Max(10) int limit,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long contractorCompanyId =
                ControllerPrincipalSupport.requireCompanyId(principal);

        return ResponseEntity.ok(
                matchingService.findMatches(
                        workPackageId,
                        contractorCompanyId,
                        limit
                )
        );
    }
}
