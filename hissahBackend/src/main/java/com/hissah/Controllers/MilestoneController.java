package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.Controllers.support.RequestValidationSupport;
import com.hissah.DTO.Request.MilestoneProgressRequestDTO;
import com.hissah.DTO.Request.MilestoneRequestDTO;
import com.hissah.DTO.Response.MilestoneResponseDTO;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.MilestoneService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MilestoneController {
    private final MilestoneService milestoneService;
    private final RequestValidationSupport requestValidationSupport;

    @PostMapping("/awards/{awardId}/milestones")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<MilestoneResponseDTO> create(
            @PathVariable @Positive Long awardId,
            @RequestBody MilestoneRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        request.setAwardId(awardId);
        requestValidationSupport.validate(request);

        MilestoneResponseDTO response =
                milestoneService.create(
                        request,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping(
            "/awards/{awardId}/milestones/{milestoneId}"
    )
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<MilestoneResponseDTO> update(
            @PathVariable @Positive Long awardId,
            @PathVariable @Positive Long milestoneId,
            @RequestBody MilestoneRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        request.setAwardId(awardId);
        requestValidationSupport.validate(request);

        return ResponseEntity.ok(
                milestoneService.update(
                        milestoneId,
                        request,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/milestones/{milestoneId}/progress")
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<MilestoneResponseDTO> updateProgress(
            @PathVariable @Positive Long milestoneId,
            @Valid @RequestBody
            MilestoneProgressRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                milestoneService.updateProgress(
                        milestoneId,
                        request,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/milestones/{milestoneId}/submit")
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<MilestoneResponseDTO> submit(
            @PathVariable @Positive Long milestoneId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                milestoneService.submit(
                        milestoneId,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/milestones/{milestoneId}/approve")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<MilestoneResponseDTO> approve(
            @PathVariable @Positive Long milestoneId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                milestoneService.approve(
                        milestoneId,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @GetMapping("/awards/{awardId}/milestones")
    @PreAuthorize(
            "hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')"
    )
    public ResponseEntity<List<MilestoneResponseDTO>> getByAward(
            @PathVariable @Positive Long awardId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                milestoneService.getByAward(
                        awardId,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        )
                )
        );
    }
}
