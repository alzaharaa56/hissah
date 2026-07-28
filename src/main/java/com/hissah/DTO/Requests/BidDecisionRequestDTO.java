package com.hissah.DTOs.Requests;

import com.hissah.Enums.BidStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidDecisionRequestDTO {

    @NotNull(message = "Decision status is required.")
    private BidStatus decision;

    @Size(max = 500, message = "Decision reason must not exceed 500 characters.")
    private String reason;

    @AssertTrue(message = "Decision must be SHORTLISTED or REJECTED.")
    public boolean isSupportedDecision() {
        return decision == null
                || decision == BidStatus.SHORTLISTED
                || decision == BidStatus.REJECTED;
    }
}
