package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.DTO.Response.DashboardResponseDTO;
import com.hissah.Enums.Role;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportingService reportingService;

    @GetMapping("/dashboard")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MAIN_CONTRACTOR','SUBCONTRACTOR')"
    )
    public ResponseEntity<DashboardResponseDTO> getDashboard(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long userId =
                ControllerPrincipalSupport.requireUserId(
                        principal
                );

        Role role =
                ControllerPrincipalSupport.requireRole(
                        principal
                );

        Long companyId =
                role == Role.ADMIN
                        ? principal.getCompanyId()
                        : ControllerPrincipalSupport.requireCompanyId(
                        principal
                );

        return ResponseEntity.ok(
                reportingService.getDashboard(
                        userId,
                        companyId,
                        role
                )
        );
    }
}
