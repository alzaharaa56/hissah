package com.hissah.Controllers;
import com.hissah.DTO.Request.CompanyRequestDTO;
import com.hissah.DTO.Request.CompanyVerificationRequestDTO;
import com.hissah.DTO.Response.CompanyResponseDTO;
import com.hissah.Enums.Role;
import com.hissah.Services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/me")
    public ResponseEntity<CompanyResponseDTO> getCurrentCompany(@RequestParam Long userId) {
        CompanyResponseDTO response = companyService.getCurrentCompany(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponseDTO> getById(
            @PathVariable Long companyId,
            @RequestParam Long currentCompanyId,
            @RequestParam Role currentRole
    ) {
        CompanyResponseDTO response = companyService.getById(companyId, currentCompanyId, currentRole);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<CompanyResponseDTO> updateCurrentCompany(
            @RequestParam Long companyId,
            @RequestBody CompanyRequestDTO request
    ) {
        CompanyResponseDTO response = companyService.updateCurrentCompany(companyId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CompanyResponseDTO>> getPendingCompanies() {
        List<CompanyResponseDTO> response = companyService.getPendingCompanies();
        return ResponseEntity.ok(response);
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