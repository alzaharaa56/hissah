package com.hissah.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardRequestDTO {

    @NotNull(message = "Selected bid ID is required.")
    private Long bidId;

    @NotNull(message = "Agreed amount is required.")
    @DecimalMin(value = "0.001", message = "Agreed amount must be greater than zero.")
    private BigDecimal agreedAmount;

    @NotNull(message = "Agreed delivery days are required.")
    @Positive(message = "Agreed delivery days must be greater than zero.")
    private Integer agreedDeliveryDays;

    @Size(max = 10000, message = "Award notes are too long.")
    private String notes;
}
