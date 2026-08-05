package com.hissah.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class AiWorkPackageRequestDTO {

    @NotBlank(message = "A short work description is required.")
    @Size(
            min = 15,
            max = 3000,
            message = "The work description must contain between 15 and 3000 characters."
    )
    private String brief;

    private Long projectId;
    private Long categoryId;

    @Size(max = 120, message = "Location must not exceed 120 characters.")
    private String location;

    @DecimalMin(value = "0.000", message = "Minimum budget cannot be negative.")
    private BigDecimal budgetMin;

    @DecimalMin(value = "0.000", message = "Maximum budget cannot be negative.")
    private BigDecimal budgetMax;

    @Future(message = "Deadline must be in the future.")
    private LocalDateTime deadline;
}
