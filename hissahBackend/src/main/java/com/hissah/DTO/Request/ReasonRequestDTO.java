package com.hissah.DTO.Request;

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
public class ReasonRequestDTO {

    @Size(max = 1000, message = "Reason must not exceed 1000 characters.")
    private String reason;
}
