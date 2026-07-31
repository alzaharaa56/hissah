package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.Controllers.support.RequestValidationSupport;
import com.hissah.DTO.Request.BidDecisionRequestDTO;
import com.hissah.DTO.Request.BidRequestDTO;
import com.hissah.DTO.Request.ReasonRequestDTO;
import com.hissah.DTO.Response.BidComparisonResponseDTO;
import com.hissah.DTO.Response.BidDocumentDownloadDTO;
import com.hissah.DTO.Response.BidResponseDTO;
import com.hissah.Enums.BidStatus;
import com.hissah.Exceptions.FileStorageException;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.BidService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class BidController {

    private final ControllerPrincipalSupport principalSupport;
    private final BidService bidService;
    private final RequestValidationSupport requestValidationSupport;

    @PostMapping(
            value = "/work-packages/{workPackageId}/bids/draft",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<BidResponseDTO> createDraft(
            @PathVariable @Positive Long workPackageId,
            @ModelAttribute BidRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        request.setWorkPackageId(workPackageId);
        requestValidationSupport.validate(request);

        BidResponseDTO response =
                bidService.createDraft(
                        request,
                        principalSupport.requireCompanyId(
                                principal
                        ),
                        principalSupport.requireUserId(
                                principal
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping(
            value = "/work-packages/{workPackageId}/bids",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<BidResponseDTO> submitNew(
            @PathVariable @Positive Long workPackageId,
            @ModelAttribute BidRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        request.setWorkPackageId(workPackageId);
        requestValidationSupport.validate(request);

        BidResponseDTO response =
                bidService.submitNew(
                        request,
                        principalSupport.requireCompanyId(
                                principal
                        ),
                        principalSupport.requireUserId(
                                principal
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping(
            value = "/bids/{bidId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<BidResponseDTO> update(
            @PathVariable @Positive Long bidId,
            @ModelAttribute BidRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long companyId =
                principalSupport.requireCompanyId(
                        principal
                );

        BidResponseDTO existing =
                bidService.getById(bidId, companyId);

        request.setWorkPackageId(
                existing.getWorkPackageId()
        );
        requestValidationSupport.validate(request);

        return ResponseEntity.ok(
                bidService.update(
                        bidId,
                        request,
                        companyId,
                        principalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/bids/{bidId}/submit")
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<BidResponseDTO> submitDraft(
            @PathVariable @Positive Long bidId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                bidService.submitDraft(
                        bidId,
                        principalSupport.requireCompanyId(
                                principal
                        ),
                        principalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/bids/{bidId}/withdraw")
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<BidResponseDTO> withdraw(
            @PathVariable @Positive Long bidId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                bidService.withdraw(
                        bidId,
                        principalSupport.requireCompanyId(
                                principal
                        ),
                        principalSupport.requireUserId(
                                principal
                        )
                )
        );
    }

    @PatchMapping("/bids/{bidId}/shortlist")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<BidResponseDTO> shortlist(
            @PathVariable @Positive Long bidId,
            @Valid @RequestBody(required = false)
            ReasonRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return decide(
                bidId,
                BidStatus.SHORTLISTED,
                request,
                principal
        );
    }

    @PatchMapping("/bids/{bidId}/reject")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<BidResponseDTO> reject(
            @PathVariable @Positive Long bidId,
            @Valid @RequestBody(required = false)
            ReasonRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return decide(
                bidId,
                BidStatus.REJECTED,
                request,
                principal
        );
    }

    @GetMapping("/bids/{bidId}")
    @PreAuthorize(
            "hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')"
    )
    public ResponseEntity<BidResponseDTO> getById(
            @PathVariable @Positive Long bidId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                bidService.getById(
                        bidId,
                        principalSupport.requireCompanyId(
                                principal
                        )
                )
        );
    }

    @GetMapping("/bids/my-bids")
    @PreAuthorize("hasRole('SUBCONTRACTOR')")
    public ResponseEntity<Page<BidResponseDTO>> getMyBids(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                bidService.getMyBids(
                        principalSupport.requireCompanyId(
                                principal
                        ),
                        pageable
                )
        );
    }

    @GetMapping("/work-packages/{workPackageId}/bids")
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<List<BidComparisonResponseDTO>>
    compareForWorkPackage(
            @PathVariable @Positive Long workPackageId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                bidService.compareForWorkPackage(
                        workPackageId,
                        principalSupport.requireCompanyId(
                                principal
                        )
                )
        );
    }


    @GetMapping("/bid-documents/{documentId}/download")
    @PreAuthorize(
            "hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')"
    )
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable @Positive Long documentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        BidDocumentDownloadDTO document =
                bidService.downloadDocument(
                        documentId,
                        principalSupport.requireCompanyId(
                                principal
                        )
                );

        try {
            Resource resource =
                    new UrlResource(
                            document.getPath().toUri()
                    );

            MediaType contentType =
                    safeContentType(
                            document.getContentType()
                    );

            ResponseEntity.BodyBuilder response =
                    ResponseEntity.ok()
                            .contentType(contentType)
                            .header(
                                    HttpHeaders.CONTENT_DISPOSITION,
                                    ContentDisposition
                                            .attachment()
                                            .filename(
                                                    document.getFileName(),
                                                    StandardCharsets.UTF_8
                                            )
                                            .build()
                                            .toString()
                            );

            if (document.getFileSize() != null
                    && document.getFileSize() >= 0) {
                response.contentLength(
                        document.getFileSize()
                );
            }

            return response.body(resource);
        } catch (MalformedURLException exception) {
            throw new FileStorageException(
                    "The bid document could not be opened.",
                    exception
            );
        }
    }


    private MediaType safeContentType(String value) {
        if (value == null || value.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(value);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private ResponseEntity<BidResponseDTO> decide(
            Long bidId,
            BidStatus status,
            ReasonRequestDTO reasonRequest,
            CustomUserPrincipal principal
    ) {
        BidDecisionRequestDTO decision =
                BidDecisionRequestDTO.builder()
                        .decision(status)
                        .reason(
                                reasonRequest == null
                                        ? null
                                        : reasonRequest.getReason()
                        )
                        .build();

        return ResponseEntity.ok(
                bidService.decide(
                        bidId,
                        decision,
                        principalSupport.requireCompanyId(
                                principal
                        ),
                        principalSupport.requireUserId(
                                principal
                        )
                )
        );
    }
}
