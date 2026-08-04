package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.DTO.Request.CompanyRequestDTO;
import com.hissah.DTO.Response.CompanyResponseDTO;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')")
    public ResponseEntity<CompanyResponseDTO> getCurrentCompany(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                companyService.getCurrentCompany(
                        ControllerPrincipalSupport.requireUserId(principal)
                )
        );
    }

    @GetMapping("/{companyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompanyResponseDTO> getById(
            @PathVariable @Positive Long companyId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                ControllerPrincipalSupport.requirePrincipal(principal);

        return ResponseEntity.ok(
                companyService.getById(
                        companyId,
                        authenticated.getCompanyId(),
                        ControllerPrincipalSupport.requireRole(authenticated)
                )
        );
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')")
    public ResponseEntity<CompanyResponseDTO> updateCurrentCompany(
            @Valid @RequestBody CompanyRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                companyService.updateCurrentCompany(
                        ControllerPrincipalSupport.requireCompanyId(principal),
                        request
                )
        );
    }
}
