package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.DTO.Request.CompanyVerificationRequestDTO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminCompanyController {

    private final CompanyService companyService;

    @GetMapping("/pending")
    public ResponseEntity<List<CompanyResponseDTO>> getPendingCompanies() {
        return ResponseEntity.ok(
                companyService.getPendingCompanies()
        );
    }

    @PostMapping("/{companyId}/verify")
    public ResponseEntity<CompanyResponseDTO> verifyCompany(
            @PathVariable @Positive Long companyId,
            @Valid @RequestBody CompanyVerificationRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                companyService.verifyCompany(
                        companyId,
                        request,
                        ControllerPrincipalSupport.requireUserId(principal)
                )
        );
    }
}
