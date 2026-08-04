package com.hissah.Services.Implementations.Support;

import com.hissah.DTO.Response.BidComparisonResponseDTO;
import com.hissah.DTO.Response.BidResponseDTO;
import com.hissah.Entities.Bid;
import com.hissah.Entities.BidDocument;
import com.hissah.Entities.Company;
import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.VerificationStatus;
import com.hissah.Repositories.BidDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BidResponseMapper {

    private final BidDocumentRepository bidDocumentRepository;
    private final CompanyReferenceSupport companyReferenceSupport;

    public BidResponseDTO toResponse(Bid bid) {
        List<BidResponseDTO.DocumentItem> documents =
                bidDocumentRepository
                        .findByBidIdAndActiveTrueOrderByCreatedAtAsc(bid.getId())
                        .stream()
                        .map(this::toDocumentItem)
                        .toList();

        Company bidder = bid.getBidderCompany();
        WorkPackage workPackage = bid.getWorkPackage();

        return BidResponseDTO.builder()
                .id(bid.getId())
                .referenceNumber(bid.getReferenceNumber())
                .amount(bid.getAmount())
                .deliveryDays(bid.getDeliveryDays())
                .proposalText(bid.getProposalText())
                .submittedAt(bid.getSubmittedAt())
                .status(bid.getStatus())
                .decisionReason(bid.getDecisionReason())
                .bidderCompanyId(bidder.getId())
                .bidderCompanyName(companyReferenceSupport.companyName(bidder))
                .bidderVerified(
                        bidder.getVerificationStatus()
                                == VerificationStatus.VERIFIED
                )
                .workPackageId(workPackage.getId())
                .workPackageReferenceNumber(workPackage.getReferenceNumber())
                .workPackageTitle(workPackage.getTitle())
                .documents(documents)
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }

    public BidComparisonResponseDTO toComparison(Bid bid, long rank) {
        Company bidder = bid.getBidderCompany();

        return BidComparisonResponseDTO.builder()
                .rank(rank)
                .bidId(bid.getId())
                .referenceNumber(bid.getReferenceNumber())
                .bidderCompanyId(bidder.getId())
                .bidderCompanyName(companyReferenceSupport.companyName(bidder))
                .bidderVerified(
                        bidder.getVerificationStatus()
                                == VerificationStatus.VERIFIED
                )
                .amount(bid.getAmount())
                .deliveryDays(bid.getDeliveryDays())
                .proposalText(bid.getProposalText())
                .status(bid.getStatus())
                .submittedAt(bid.getSubmittedAt())
                .documentCount(
                        bidDocumentRepository.countByBidIdAndActiveTrue(
                                bid.getId()
                        )
                )
                .build();
    }

    private BidResponseDTO.DocumentItem toDocumentItem(
            BidDocument document
    ) {
        return BidResponseDTO.DocumentItem.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .documentType(document.getDocumentType())
                .uploadedAt(document.getCreatedAt())
                .build();
    }
}
