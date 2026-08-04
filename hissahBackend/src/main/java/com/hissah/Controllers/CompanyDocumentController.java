package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.Controllers.support.RequestValidationSupport;
import com.hissah.DTO.Request.CompanyDocumentRequestDTO;
import com.hissah.DTO.Response.CompanyDocumentResponseDTO;
import com.hissah.Enums.Role;
import com.hissah.Exceptions.UnauthorizedOperationException;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.CompanyDocumentService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class CompanyDocumentController {

    private final CompanyDocumentService companyDocumentService;
    private final RequestValidationSupport requestValidationSupport;

    @PostMapping(
            value = "/companies/{companyId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')")
    public ResponseEntity<CompanyDocumentResponseDTO> upload(
            @PathVariable @Positive Long companyId,
            @ModelAttribute CompanyDocumentRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long currentCompanyId =
                ControllerPrincipalSupport.requireCompanyId(principal);

        if (!companyId.equals(currentCompanyId)) {
            throw new UnauthorizedOperationException(
                    "You cannot upload documents for another company."
            );
        }

        requestValidationSupport.validate(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        companyDocumentService.upload(
                                companyId,
                                request
                        )
                );
    }

    @GetMapping("/companies/{companyId}/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CompanyDocumentResponseDTO>> getForCompany(
            @PathVariable @Positive Long companyId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                ControllerPrincipalSupport.requirePrincipal(principal);

        return ResponseEntity.ok(
                companyDocumentService.getForCompany(
                        companyId,
                        authenticated.getCompanyId(),
                        ControllerPrincipalSupport.requireRole(authenticated)
                )
        );
    }

    @GetMapping("/company-documents/{documentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @PathVariable @Positive Long documentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CustomUserPrincipal authenticated =
                ControllerPrincipalSupport.requirePrincipal(principal);

        CompanyDocumentService.DownloadFile file =
                companyDocumentService.download(
                        documentId,
                        authenticated.getCompanyId(),
                        ControllerPrincipalSupport.requireRole(authenticated)
                );

        try {
            Resource resource = new UrlResource(file.path().toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.contentType()))
                    .contentLength(file.fileSize())
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(
                                            file.fileName(),
                                            StandardCharsets.UTF_8
                                    )
                                    .build()
                                    .toString()
                    )
                    .body(resource);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException(
                    "The stored company document path is invalid.",
                    exception
            );
        }
    }

    @DeleteMapping("/company-documents/{documentId}")
    @PreAuthorize("hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive Long documentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        companyDocumentService.delete(
                documentId,
                ControllerPrincipalSupport.requireCompanyId(principal)
        );
        return ResponseEntity.noContent().build();
    }
}
