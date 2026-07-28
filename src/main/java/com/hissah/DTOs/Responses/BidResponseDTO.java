package com.hissah.DTOs.Responses;

import com.hissah.Enums.BidStatus;
import com.hissah.Enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponseDTO {
    private Long id;
    private String referenceNumber;
    private BigDecimal amount;
    private Integer deliveryDays;
    private String proposalText;
    private LocalDateTime submittedAt;
    private BidStatus status;
    private String decisionReason;
    private Long bidderCompanyId;
    private String bidderCompanyName;
    private Boolean bidderVerified;
    private Long workPackageId;
    private String workPackageReferenceNumber;
    private String workPackageTitle;

    @Builder.Default
    private List<DocumentItem> documents = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentItem {
        private Long id;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private DocumentType documentType;
        private LocalDateTime uploadedAt;
    }
}
