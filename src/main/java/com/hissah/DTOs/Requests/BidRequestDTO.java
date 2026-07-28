package com.hissah.DTOs.Requests;

import com.hissah.enums.DocumentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequestDTO {

    @NotNull(message = "Work package ID is required.")
    private Long workPackageId;

    @NotNull(message = "Bid amount is required.")
    @DecimalMin(value = "0.001", message = "Bid amount must be greater than zero.")
    private BigDecimal amount;

    @NotNull(message = "Delivery days are required.")
    @Positive(message = "Delivery days must be greater than zero.")
    private Integer deliveryDays;

    @NotBlank(message = "Proposal text is required.")
    @Size(max = 15000, message = "Proposal text is too long.")
    private String proposalText;

    @Builder.Default
    private List<MultipartFile> documents = new ArrayList<>();

    @Builder.Default
    private List<DocumentType> documentTypes = new ArrayList<>();

    @AssertTrue(message = "Each uploaded document must have a matching document type.")
    public boolean isDocumentTypeCountValid() {
        return documents == null
                || documents.isEmpty()
                || (documentTypes != null && documentTypes.size() == documents.size());
    }
}
