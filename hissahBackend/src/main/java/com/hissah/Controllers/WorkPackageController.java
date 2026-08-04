package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.Controllers.support.RequestValidationSupport;
import com.hissah.DTO.Request.ReasonRequestDTO;
import com.hissah.DTO.Request.WorkPackageRequestDTO;
import com.hissah.DTO.Request.WorkPackageSearchRequestDTO;
import com.hissah.DTO.Response.WorkPackageResponseDTO;
import com.hissah.DTO.Response.WorkPackageSummaryResponseDTO;
import com.hissah.Enums.Role;
import com.hissah.Enums.WorkPackageStatus;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.WorkPackageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class WorkPackageController {

    private static final Set<WorkPackageStatus> OWNER_ONLY_STATUSES =
            Set.of(
                    WorkPackageStatus.DRAFT,
                    WorkPackageStatus.CANCELLED
            );
    private final WorkPackageService workPackageService;
    private final RequestValidationSupport requestValidationSupport;

    @PostMapping("/projects/{projectId}/packages")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<WorkPackageResponseDTO> create(
            @PathVariable @Positive Long projectId,
            @RequestBody WorkPackageRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        request.setProjectId(projectId);
        requestValidationSupport.validate(request);

        WorkPackageResponseDTO response =
                workPackageService.create(
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

    @PutMapping("/work-packages/{workPackageId}")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<WorkPackageResponseDTO> update(
            @PathVariable @Positive Long workPackageId,
            @Valid @RequestBody WorkPackageRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                workPackageService.update(
                        workPackageId,
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

    @PatchMapping("/work-packages/{workPackageId}/publish")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<WorkPackageResponseDTO> publish(
            @PathVariable @Positive Long workPackageId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                workPackageService.publish(
                        workPackageId,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/work-packages/{workPackageId}/close")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<WorkPackageResponseDTO> close(
            @PathVariable @Positive Long workPackageId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                workPackageService.close(
                        workPackageId,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/work-packages/{workPackageId}/cancel")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<WorkPackageResponseDTO> cancel(
            @PathVariable @Positive Long workPackageId,
            @Valid @RequestBody(required = false)
            ReasonRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        String reason =
                request == null ? null : request.getReason();

        return ResponseEntity.ok(
                workPackageService.cancel(
                        workPackageId,
                        reason,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        ),
                        ControllerPrincipalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @GetMapping("/work-packages")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<WorkPackageSummaryResponseDTO>> search(
            @Valid @ModelAttribute
            WorkPackageSearchRequestDTO request
    ) {
        return ResponseEntity.ok(
                workPackageService.search(request)
        );
    }

    @GetMapping("/work-packages/{workPackageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkPackageResponseDTO> getById(
            @PathVariable @Positive Long workPackageId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                ControllerPrincipalSupport.requirePrincipal(
                        principal
                );
        Long companyId = authenticated.getCompanyId();

        WorkPackageResponseDTO response =
                workPackageService.getById(
                        workPackageId,
                        companyId
                );

        verifyRestrictedPackageVisibility(
                response,
                authenticated
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/packages")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<List<WorkPackageSummaryResponseDTO>>
    getByProject(
            @PathVariable @Positive Long projectId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                workPackageService.getByProject(
                        projectId,
                        ControllerPrincipalSupport.requireCompanyId(
                                principal
                        )
                )
        );
    }

    private void verifyRestrictedPackageVisibility(
            WorkPackageResponseDTO workPackage,
            CustomUserPrincipal principal
    ) {
        Role role =
                ControllerPrincipalSupport.requireRole(principal);

        if (!OWNER_ONLY_STATUSES.contains(
                workPackage.getStatus())) {
            return;
        }

        boolean admin = role == Role.ADMIN;
        boolean owner =
                principal.getCompanyId() != null
                        && principal.getCompanyId().equals(
                        workPackage.getContractorCompanyId()
                );

        if (!admin && !owner) {
            throw new UnauthorizedOperationException(
                    "You cannot view this draft or cancelled work package."
            );
        }
    }
}
