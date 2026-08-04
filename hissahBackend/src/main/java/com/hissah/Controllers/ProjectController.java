package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.DTO.Request.ProjectRequestDTO;
import com.hissah.DTO.Request.ReasonRequestDTO;
import com.hissah.DTO.Response.ProjectResponseDTO;
import com.hissah.DTO.Response.ProjectSummaryResponseDTO;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<ProjectResponseDTO> create(
            @Valid @RequestBody ProjectRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProjectResponseDTO response = projectService.create(
                request,
                ControllerPrincipalSupport.requireCompanyId(principal),
                ControllerPrincipalSupport.requireUserId(principal)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<ProjectResponseDTO> update(
            @PathVariable @Positive Long projectId,
            @Valid @RequestBody ProjectRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                projectService.update(
                        projectId,
                        request,
                        ControllerPrincipalSupport.requireCompanyId(principal),
                        ControllerPrincipalSupport.requireUserId(principal)
                )
        );
    }

    @PatchMapping("/{projectId}/activate")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<ProjectResponseDTO> activate(
            @PathVariable @Positive Long projectId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                projectService.activate(
                        projectId,
                        ControllerPrincipalSupport.requireCompanyId(principal),
                        ControllerPrincipalSupport.requireUserId(principal)
                )
        );
    }

    @PatchMapping("/{projectId}/complete")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<ProjectResponseDTO> complete(
            @PathVariable @Positive Long projectId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                projectService.complete(
                        projectId,
                        ControllerPrincipalSupport.requireCompanyId(principal),
                        ControllerPrincipalSupport.requireUserId(principal)
                )
        );
    }

    @PatchMapping("/{projectId}/cancel")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<ProjectResponseDTO> cancel(
            @PathVariable @Positive Long projectId,
            @RequestBody(required = false) ReasonRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                projectService.cancel(
                        projectId,
                        request == null ? null : request.getReason(),
                        ControllerPrincipalSupport.requireCompanyId(principal),
                        ControllerPrincipalSupport.requireUserId(principal)
                )
        );
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponseDTO> getById(
            @PathVariable @Positive Long projectId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                ControllerPrincipalSupport.requirePrincipal(principal);

        return ResponseEntity.ok(
                projectService.getById(
                        projectId,
                        authenticated.getCompanyId(),
                        ControllerPrincipalSupport.requireRole(authenticated)
                )
        );
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<Page<ProjectSummaryResponseDTO>> getMyProjects(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                projectService.getMyProjects(
                        ControllerPrincipalSupport.requireCompanyId(principal),
                        pageable
                )
        );
    }
}
