package com.hissah.DTO.Request;

import com.hissah.Enums.VerificationStatus;
import lombok.Data;

@Data
public class CompanyVerificationRequestDTO {
    private VerificationStatus verificationStatus;
    private String reason;
}
