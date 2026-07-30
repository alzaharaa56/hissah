package com.hissah.Controllers;

import com.hissah.DTO.Request.CompanyVerificationRequestDTO;
import com.hissah.DTO.Response.CompanyResponseDTO;
import com.hissah.Services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final CompanyService companyService;

    @GetMapping("/pending")
    public ResponseEntity<List<CompanyResponseDTO>> getPendingCompanies() {
        List<CompanyResponseDTO> pendingCompanies = companyService.getPendingCompanies();
        return ResponseEntity.ok(pendingCompanies);
    }

    @PostMapping("/{companyId}/verify")
    public ResponseEntity<CompanyResponseDTO> verifyCompany(
            @PathVariable Long companyId,
            @RequestBody CompanyVerificationRequestDTO request,
            @RequestParam Long adminUserId
    ) {
        CompanyResponseDTO response = companyService.verifyCompany(companyId, request, adminUserId);
        return ResponseEntity.ok(response);
    }
}
