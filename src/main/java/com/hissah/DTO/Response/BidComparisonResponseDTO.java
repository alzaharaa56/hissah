package com.hissah.DTO.Response;

import com.hissah.Enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidComparisonResponseDTO {
    private Long rank;
    private Long bidId;
    private String referenceNumber;
    private Long bidderCompanyId;
    private String bidderCompanyName;
    private Boolean bidderVerified;
    private BigDecimal amount;
    private Integer deliveryDays;
    private String proposalText;
    private BidStatus status;
    private LocalDateTime submittedAt;
    private Long documentCount;
}
