package com.hissah.Controllers;

import com.hissah.Controllers.support.ControllerPrincipalSupport;
import com.hissah.Controllers.support.RequestValidationSupport;
import com.hissah.DTO.Request.AwardRequestDTO;
import com.hissah.DTO.Response.AwardResponseDTO;
import com.hissah.DTO.Response.BidResponseDTO;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Security.CustomUserPrincipal;
import com.hissah.Services.AwardService;
import com.hissah.Services.BidService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AwardController {

    private final ControllerPrincipalSupport principalSupport;
    private final AwardService awardService;
    private final BidService bidService;
    private final RequestValidationSupport requestValidationSupport;

    @PostMapping(
            "/work-packages/{workPackageId}/award/{bidId}"
    )
    @PreAuthorize("hasRole('MAIN_CONTRACTOR')")
    public ResponseEntity<AwardResponseDTO> awardBid(
            @PathVariable @Positive Long workPackageId,
            @PathVariable @Positive Long bidId,
            @RequestBody AwardRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long contractorCompanyId =
                principalSupport.requireCompanyId(
                        principal
                );

        BidResponseDTO selectedBid =
                bidService.getById(
                        bidId,
                        contractorCompanyId
                );

        if (!workPackageId.equals(
                selectedBid.getWorkPackageId())) {
            throw new BusinessRuleException(
                    "The selected bid does not belong to the specified work package."
            );
        }

        request.setBidId(bidId);
        requestValidationSupport.validate(request);

        AwardResponseDTO response =
                awardService.awardBid(
                        request,
                        contractorCompanyId,
                        principalSupport.requireUserId(
                                principal
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/awards/{awardId}")
    @PreAuthorize(
            "hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')"
    )
    public ResponseEntity<AwardResponseDTO> getById(
            @PathVariable @Positive Long awardId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                awardService.getById(
                        awardId,
                        principalSupport.requireCompanyId(
                                principal
                        )
                )
        );
    }

    @GetMapping("/work-packages/{workPackageId}/award")
    @PreAuthorize(
            "hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')"
    )
    public ResponseEntity<AwardResponseDTO> getByWorkPackage(
            @PathVariable @Positive Long workPackageId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                awardService.getByWorkPackage(
                        workPackageId,
                        principalSupport.requireCompanyId(
                                principal
                        )
                )
        );
    }

    @GetMapping("/awards/my-awards")
    @PreAuthorize(
            "hasAnyRole('MAIN_CONTRACTOR','SUBCONTRACTOR')"
    )
    public ResponseEntity<List<AwardResponseDTO>> getMyAwards(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                awardService.getMyAwards(
                        principalSupport.requireCompanyId(
                                principal
                        )
                )
        );
    }
}
